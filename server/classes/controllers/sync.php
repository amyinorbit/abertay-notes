<?php

namespace controllers;

class sync {
    
    private static $insert = <<<EOT
    INSERT INTO `note`
    (`userID`, `uniqueID`, `text`, `sortDate`, `createDate`, `syncDate`)
    VALUES (:userID, :uniqueID, :text, :sortDate, :createDate, :syncDate)
    ON DUPLICATE KEY UPDATE
    `text` = IF(sortDate < VALUES(sortDate), VALUES(text), text),
    `syncDate` = IF(sortDate < VALUES(sortDate), VALUES(syncDate), syncDate),
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
        `syncDate` > :syncDate;
EOT;

    private static $delete = <<<EOT
    DELETE FROM `note`
    WHERE `userID` = :userID AND `uniqueID` = :uniqueID;
EOT;

    private static $insertDeleted = <<<EOT
    INSERT INTO `deletedNote`
    (`userID`, `uniqueID`)
    VALUES (:userID, :uniqueID);
EOT;

    private static $selDelete = <<<EOT
    SELECT `uniqueID` FROM `deletedNote` WHERE `userID` = :userID AND `deleteDate` > :deleteDate;
EOT;
            
    private static $NoteFields = ["uniqueID", "text", "sortDate", "createDate"];
    
    public function __construct() {
        
    }
    
    /**
     * Process an update request, and returns the changes that have occurred since the sync
     * time sent with the request.
     */
    public function Update($req, $res) {
        $db = \app::Connection();
        
        $clientTime = $req->Header("X-NetNotes-Time", \utils::Date(0));
        $transaction = json_decode($req->Body(), true);
        if(is_null($transaction) || !is_array($transaction)) {
            return $this->_InvalidFormat($res);
        }
        
        $db->beginTransaction();
        $stmt = $db->prepare(self::$insert);
        foreach($transaction as $note) {
            if($this->_ValidateNote($note)) {
                return $this->_InvalidFormat();
            }
            $stmt->execute([
                "userID" => \app::UserID(),
                "uniqueID" => $note["uniqueID"],
                "text" => $note["text"],
                "sortDate" => $note["sortDate"],
                "createDate" => $note["createDate"],
                "syncDate" => $clientTime,
            ]);
        }
        $db->commit();
        
        $stmt = $db->prepare(self::$selInsert);
        if(!$stmt->execute(["userID" => \app::UserID(), "syncDate" => $clientTime])) {
            throw new \Exception("Database Error");
        }
        $res->SetBody(["changes" => $stmt->fetchAll(\PDO::FETCH_ASSOC)]);
        $res->SetStatusCode(200);
    }
    
    /**
     * Process an delete request, and returns the deletions that have occurred since the sync
     * time sent with the request.
     */
    public function Delete($req, $res) {
        $db = \app::Connection();
        
        $clientTime = $req->Header("X-NetNotes-Time", 0);
        $transaction = json_decode($req->Body(), true);
        if(is_null($transaction) || !is_array($transaction)) {
            return $this->_InvalidFormat($res);
        }
        
        $db->beginTransaction();
        $stmt1 = $db->prepare(self::$delete);
        $stmt2 = $db->prepare(self::$insertDeleted);
        foreach($transaction as $uuid) {
            if(!is_string($uuid)) {
                return $this->_InvalidFormat($res);
            }
            $stmt1->execute(["userID" => \app::UserID(), "uniqueID" => $uuid]);
            $stmt2->execute(["userID" => \app::UserID(), "uniqueID" => $uuid]);
        }
        $db->commit();
        
        $stmt = $db->prepare(self::$selDelete);
        if(!$stmt->execute(["userID" => \app::UserID(), "deleteDate" => $clientTime])) {
            throw new \Exception("Database Error");
        }
        $deleted = [];
        while($result = $stmt->fetch(\PDO::FETCH_ASSOC)) {
            array_push($deleted, $result["uniqueID"]);
        }
        $res->SetBody(["changes" => $deleted]);
        $res->SetStatusCode(200);
    }
    
    private function _InvalidFormat($res) {
        $res->SetStatusCode(422);
        $res->SetBody(["fail" => "Invalid Entity."]);
    }
    
    public function _ValidateNote($note) {
        if(!is_array($note)) { return false; }
        
    }
}