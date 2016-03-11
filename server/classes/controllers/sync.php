<?php

namespace controllers;

class sync {
    
    private static $insert = <<<EOT
    INSERT INTO `note`
    (`userID`, `uniqueID`, `text`, `sortDate`, `createDate`, `seqID`)
    VALUES (:userID, :uniqueID, :text, :sortDate, :createDate, :seqID)
    ON DUPLICATE KEY UPDATE
    `text` = IF(sortDate < VALUES(sortDate), VALUES(text), text),
    `seqID` = IF(sortDate < VALUES(sortDate), VALUES(seqID), seqID),
    `sortDate` = IF(sortDate < VALUES(sortDate), VALUES(sortDate), sortDate);
EOT;

    private static $selInsert = <<<EOT
    SELECT
        `uniqueID`,
        DATE_FORMAT(`createDate`, '%Y-%m-%d %T+0000') AS createDate,
        DATE_FORMAT(`sortDate`, '%Y-%m-%d %T+0000') AS sortDate,
        `text`
    FROM `note`
    WHERE
        `userID` = :userID AND
        `seqID` > :oldSeqID AND
        `seqID` <= :newSeqID;
EOT;

    private static $delete = <<<EOT
    DELETE FROM `note`
    WHERE `userID` = :userID AND `uniqueID` = :uniqueID;
EOT;

    private static $insertDeleted = <<<EOT
    INSERT IGNORE INTO `deletedNote`
    (`userID`, `uniqueID`, `seqID`)
    VALUES (:userID, :uniqueID, :seqID);
EOT;

    private static $selDelete = <<<EOT
    SELECT `uniqueID` FROM `deletedNote`
    WHERE `userID` = :userID AND
    `seqID` > :oldSeqID AND
    `seqID` <= :newSeqID;
EOT;
    
    private static $selID = "SELECT `%s` as 'id' FROM `user` WHERE `uniqueID` = :userID;";
    
    private static $incID = "UPDATE `user` SET `%s` = :seqID WHERE `uniqueID` = :userID;";
            
    private static $NoteFields = ["uniqueID", "text", "sortDate", "createDate"];
    
    public function __construct() {
        
    }
    
    /**
     * Process an update request, and returns the changes that have occurred since the sync
     * time sent with the request.
     */
    public static function Update($req, $res) {
        $db = \app::Connection();
        $since = $req->Header("X-NetNotes-Transaction", 0);
        $newID = self::UpdateSeqID(\app::UserID());
        $seqID = $newID;
        
        $transaction = json_decode($req->Body(), true);
        if(is_null($transaction) || !is_array($transaction)) {
            return self::_InvalidFormat($res);
        }
        if(count($transaction) > 0) {
            $newID += 1;
            $db->beginTransaction();
            $stmt = $db->prepare(self::$insert);
            foreach($transaction as $note) {
                if(self::_ValidateNote($note)) {
                    $db->rollBack();
                    return self::_InvalidFormat();
                }
                $stmt->execute([
                    "userID" => \app::UserID(),
                    "uniqueID" => $note["uniqueID"],
                    "text" => $note["text"],
                    "sortDate" => $note["sortDate"],
                    "createDate" => $note["createDate"],
                    "seqID" => $newID,
                ]);
            }
            self::SetUpdateSeqID(\app::UserID(), $newID);
            $db->commit();
        }
        
        $stmt = $db->prepare(self::$selInsert);
        if(!$stmt->execute([
            "userID" => \app::UserID(),
            "oldSeqID" => $since,
            "newSeqID" => $seqID
        ])) {
            throw new \Exception("Database Error");
        }
        $res->SetHeader("X-NetNotes-Transaction", strval($newID));
        $res->SetBody(["changes" => $stmt->fetchAll(\PDO::FETCH_ASSOC)]);
        $res->SetStatusCode(200);
        return (count($transaction) > 0);
    }
    
    /**
     * Process an delete request, and returns the deletions that have occurred since the sync
     * time sent with the request.
     */
    public static function Delete($req, $res) {
        $db = \app::Connection();
        $since = $req->Header("X-NetNotes-Transaction", 0);
        $newID = self::DeleteSeqID(\app::UserID());
        $seqID = $newID;
        
        $transaction = json_decode($req->Body(), true);
        if(is_null($transaction) || !is_array($transaction)) {
            return self::_InvalidFormat($res);
        }
        
        if(count($transaction) > 0) {
            $newID += 1;
            $db->beginTransaction();
            $stmt1 = $db->prepare(self::$delete);
            $stmt2 = $db->prepare(self::$insertDeleted);
            foreach($transaction as $uuid) {
                if(!is_string($uuid)) {
                    return self::_InvalidFormat($res);
                }
                $stmt1->execute(["userID" => \app::UserID(), "uniqueID" => $uuid]);
                $stmt2->execute([
                    "userID" => \app::UserID(),
                    "uniqueID" => $uuid,
                    "seqID" => $newID
                ]);
            }
            self::SetDeleteSeqID(\app::UserID(), $newID);
            $db->commit();
        }
        
        $stmt = $db->prepare(self::$selDelete);
        if(!$stmt->execute([
            "userID" => \app::UserID(),
            "oldSeqID" => $since,
            "newSeqID" => $seqID
        ])) {
            throw new \Exception("Database Error");
        }
        $deleted = [];
        while($result = $stmt->fetch(\PDO::FETCH_ASSOC)) {
            array_push($deleted, $result["uniqueID"]);
        }
        
        $res->SetHeader("X-NetNotes-Transaction", strval($newID));
        $res->SetBody(["changes" => $deleted]);
        $res->SetStatusCode(200);
        return (count($transaction) > 0);
    }
    
    private static function UpdateSeqID($userID) {
        return self::ID("updateSeqID", $userID);
    }
    
    private static function DeleteSeqID($userID) {
        return self::ID("deleteSeqID", $userID);
    }
    
    private static function SetUpdateSeqID($userID, $id) {
        self::SetID("updateSeqID", $id, $userID);
    }
    
    private static function SetDeleteSeqID($userID, $id) {
        self::SetID("deleteSeqID", $id, $userID);
    }
    
    private static function ID($field, $userID) {
        $db = \app::Connection();
        $get = $db->prepare(sprintf(self::$selID, $field));
        $get->execute(["userID" => $userID]);
        $result = $get->fetch(\PDO::FETCH_ASSOC);
        if(!$result) {
            throw new \Exception("Error fetching database sequence ID");
        }
        return $result["id"];
    }
    
    private static function SetID($field, $id, $userID) {
        $db = \app::Connection();
        $set = $db->prepare(sprintf(self::$incID, $field));
        if(!$set->execute(["userID" => $userID, "seqID" => $id])) {
            throw new \Exception("Error setting database sequence ID");
        }
    }
    
    private static function _InvalidFormat($res) {
        $res->SetStatusCode(422);
        $res->SetBody(["fail" => "Invalid Entity."]);
        return false;
    }
    
    public static function _ValidateNote($note) {
        if(!is_array($note)) { return false; }
    }
}