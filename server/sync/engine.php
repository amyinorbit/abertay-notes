<?php
/**
 * Engine.php - Synchronisation engine for the note-taking app.
 * 
 * @version     1.0a1
 * @author      Cesar Parent <cesar@cesarparent.com>
 * @package     sync
 */
namespace sync;

require_once(__DIR__."/utils.php");

class engine {
    
    private $_options = [
        "api_key" => "",
        "timezone" => "UTC",
        "base_url" => "http://localhost:8888",
        "database" => "db.db"
    ];
    
    private $_conn;
    
    public function __construct($configFile) {
        
        
        // parse configuration
        $data = file_get_contents($configFile);
        if($data === false) {
            throw new \Exception("Error reading config file: ".$configFile);
        }
        $options = json_decode($data, true);
        if(is_null($options)) {
            throw new \Exception("Invalid JSON configuration file");
        }
        $this->_options = array_merge($this->_options, $options);
        
        date_default_timezone_set($this->_options["timezone"]);
        ini_set('display_errors', 1);
        ini_set('display_startup_errors', 1);
        error_reporting(E_ALL);
        
        // connect to the database
        $this->_conn = new \PDO("sqlite:".dirname($configFile)."/".$this->_options["database"]);
        $this->_conn->setAttribute(\PDO::ATTR_ERRMODE, \PDO::ERRMODE_EXCEPTION);
    }
    
    
    public function processRequest($req, $res) {
        $request = json_decode($req->body(), true);
        if(is_null($request)) {
            $res->setStatusCode(422);
            $res->setBody([
                "status" => "error",
                "message" => "Invalid request body (should be JSON)"
            ]);
            return;
        }
        if(!$this->_execRequest($request)) {
            $res->setStatusCode(400);
            $res->setBody([
                "status" => "error",
                "message" => "Missing entities in request body."
            ]);
            return;
        }
        $changeSet = $this->_createResponse($request);
        if($changeSet === false) {
            $res->setStatusCode(400);
            $res->setBody([
                "status" => "error"
            ]);
           return;
        }
        $res->setStatusCode(200);
        $res->setBody([
            "time" => date("Y-m-d H:i:sO"),
            "changeSet" => $changeSet
        ]);
    }
    
    private function _execRequest($request) {
        // process additions first
        if(!isset($request["create"]) || !isset($request["update"]) || !isset($request["delete"])){
            return false;
        }
        if(!$this->_doCreate($request["create"])) { return false; }
        if(!$this->_doUpdate($request["update"])) { return false; }
        if(!$this->_doDelete($request["delete"])) { return false; }
        return true;
    }
    
    private function _createResponse($request) {
        //if(!isset($request["time"])) { return false; }
        $clientTime = $request["time"];
        //$serverTime = date("Y-m-d H:i:s O");
        $changeSet = [
            "create" => [],
            "update" => [],
            "delete" => []
        ];
        
        $stmt = $this->_conn->prepare("SELECT `uniqueID`, `text`, `createDate`, `sortDate`
                                       FROM `note`
                                       WHERE `createDate` > :clientTime
                                       AND `userID` = 1");
        if(!$stmt->execute(["clientTime" => $clientTime])) { return false; }
        while($addition = $stmt->fetch(\PDO::FETCH_ASSOC)) {
            array_push($changeSet["create"], $addition);
        }
        
        $stmt = $this->_conn->prepare("SELECT `uniqueID`, `text`, `createDate`, `sortDate`
                                       FROM `note`
                                       WHERE `sortDate` > :clientTime
                                       AND `createDate` < :clientTime
                                       AND `userID` = 1");
        if(!$stmt->execute(["clientTime" => $clientTime])) { return false; }
        while($update = $stmt->fetch(\PDO::FETCH_ASSOC)) {
           array_push($changeSet["update"], $update);
        }
       
        $stmt = $this->_conn->prepare("SELECT `uniqueID`
                                      FROM `deletedNote`
                                      WHERE `deleteDate` > :clientTime
                                      AND `userID` = 1");
        if(!$stmt->execute(["clientTime" => $clientTime])) { return false; }
        while($update = $stmt->fetch(\PDO::FETCH_ASSOC)) {
          array_push($changeSet["update"], $update);
        }
        return $changeSet;
    }
    
    private function _doCreate($additions) {
        $stmt = $this->_conn->prepare("INSERT INTO `note`
                                      (`uniqueID`, `userID`, `text`, `createDate`, `sortDate`)
                                      VALUES (:uuid, :uid, :text, :createDate, :modDate)");
        foreach($additions as $note) {
            // to make sure nothing else sneaks in.
            $params = [
                "uuid" =>       $note["uniqueID"],
                "uid" =>        1,
                "text" =>       $note["text"],
                "createDate" => $note["createDate"],
                "modDate" =>    $note["createDate"]
            ];
            if(!$stmt->execute($params)) {
                return false;
            }
        }
        return true;
    }
    
    private function _doUpdate($updates) {
        $stmt = $this->_conn->prepare("UPDATE `note`
                                       SET `sortDate`=:modDate, `text`=:text
                                       WHERE `uniqueID`=:uuid AND
                                             `sortDate` < :modDate AND
                                             `userID` = :uid;");
        foreach($updates as $note) {
            $params = [
                "modDate" =>    $note["modDate"],
                "text" =>       $note["text"],
                "uuid" =>       $note["uniqueID"],
                "uid" =>     1
            ];
            if(!$stmt->execute($params)) {
                return false;
            }
        }
        return true;
    }
    
    private function _doDelete($updates) {
        return true;
    }
}