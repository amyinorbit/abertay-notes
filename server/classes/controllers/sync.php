<?php

namespace controllers;

// TODO: Only replace if the new version is newer. Might require MySQL
$insert = <<<EOT
INSERT OR REPLACE INTO `note`
(`userID`, `uniqueID`, `text`, `sortDate`, `createDate`)
VALUES (?, ?, ?, ?, ?);
EOT;

$delete = <<<EOT
DELETE FROM `note`
WHERE `userID` = :userID AND `uniqueID` = :uniqueID;

INSERT INTO deletedNote
(`userID`, `uniqueID`)
VALUES (:userID, :uniqueID);
EOT;

class sync {
            
    private static NoteFields = ["uniqueID", "text", "sortDate"];
    
    public function __construct() {
        
    }
    
    public function Update($req, $res) {
        $db = \app::Connection();
        
        $clientTime = $req->GetHeader("X-NetNotes-Time", 0);
        $transaction = json_decode($req);
        if(is_null($transaction) || !is_array($transaction)) {
            return $this->_InvalidFormat($res);
        }
        
        $db->beginTransaction();
        $stmt = $db->prepare($insert);
        
        foreach($transaction as $note) {
            if($this->_ValidateNote($note)) {
                return $this->_InvalidFormat();
            }
            $stmt->execute([
                \app::UserID(),
                $note["uniqueID"],
                $note["text"],
                $note["sortDate"],
                $note["createDate"]
            ]);
        }
        $db->commit();
    }
    
    
    public function Delete($req, $res) {
        $db = \app::Connection();
    }
    
    private function _InvalidFormat($res) {
        $res->SetStatusCode(422);
        $res->SetBody(["fail" => "Invalid Entity."]);
    }
    
    public function _ValidateNote($note) {
        if(!is_array($note)) { return false; }
    }
    
}