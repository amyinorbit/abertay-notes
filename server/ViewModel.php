<?php
/**
 *
 *
 *
 */
namespace CesarParent\ViewModel;
require_once(__DIR__."/REST/Engine.php");
require_once(__DIR__."/Note.php");
use CesarParent\REST;
use CesarParent\Model;

class ViewModel {
    
    private $app;
    private $conn;
    
    public function __construct(REST\Engine $app) {
        $this->app = $app;
        try {
            $this->conn = new \PDO("sqlite:".__DIR__."/notes.db");
            $this->conn->setAttribute(\PDO::ATTR_ERRMODE, \PDO::ERRMODE_EXCEPTION);
        } catch (\PDOException $e) {
            //
        }
    }
    
    /**
     * Returns true if the authentication is valid.
     */
    public function auth() {
        return true;
    }
    
    /**
     * Responds with a list of notes. 
     */
    public function get_notes() {
        $sql = "SELECT * FROM note WHERE deleted = 0 ORDER BY mod_date DESC";
        $query = $this->conn->prepare($sql);
        if(!$query->execute()) {
            throw new \Exception("SQL Error");
        }
        $notes = [];
        while($record = $query->fetch(\PDO::FETCH_ASSOC)) {
            $note = Model\Note::from_array($record);
            unset($note->content);
            array_push($notes, $note);
        }
        
        $response = REST\Response::success();
        $response->put_payload("notes", $notes);
        return $response;
    }
    
    /**
     * Responds with a single question.
     */
    public function get_note($id) {
        $sql = "SELECT * FROM note WHERE uuid = :uuid";
        $query = $this->conn->prepare($sql);
        if(!$query->execute(["uuid" => $id])) {
            throw new \Exception("SQL Error");
        }
        if($result = $query->fetch(\PDO::FETCH_ASSOC)) {
            $response = REST\Response::success();
            $response->put_payload("note", Model\Note::from_array($result));
            return $response;
        }
        return REST\Response::fail(404);
    }
    
    /**
     * Creates a question;
     *
     */
    public function create_note() {
        
    }
    
    public function edit_note($id) {
        if(!$this->question_exists($id)) {
            return REST\Response::fail(404);
        }
    }
    
    public function delete_note($id) {
        if(!$this->question_exists($id)) {
            return REST\Response::fail(404);
        }
        $sql = "UPDATE note SET deleted = 1 WHERE uuid = :uuid";
        $query = $this->conn->prepare($sql);
        
        $response = REST\Response::success();
        $response->put_payload("success", $query->execute(["uuid" => $id]));
        return $response;
    }
    
    public function authenticate() {
        $token = $this->app->get_header("Authorization");
        if(is_null($token)) { return null; }
        if(strpos(strtolower($token), "basic") !== 0) { return null; }
        $token = base64_decode(substr($token, 6));
        list($user, $password) = explode(":", $token);
        return $password === $this->app->get_option("api_key");
    }
    
    private function question_exists($id) {
        $sql = "SELECT COUNT(*) FROM note WHERE deleted = 0 AND uuid = :uuid";
        $query = $this->conn->prepare($sql);
        if(!$query->execute(["uuid" => $id])) {
            throw new \Exception("SQL Error");
        }
        return (intval($query->fetch()[0]) > 0);
    }
    
    private function fetch_question($id) {
        
    }
}