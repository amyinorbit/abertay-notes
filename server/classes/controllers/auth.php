<?php

namespace controllers;
class auth {
    
    private static $userInsert = <<<EOT
        INSERT INTO user (email, salt, hash) VALUES (:email, :salt, :hash);
EOT;
    
    /**
     * Adds a user to the system.
     * TODO: Change to Authorization header
     */
    public static function AddUser($req, $res) {
        $device = $req->Header("X-NetNotes-DeviceID");
        $email = $req->BasicUser();
        $password = $req->BasicPassword();
        if(is_null($device) || is_null($email) || is_null($password)) {
            return self::_Unauthorized($res);
        }
        
        if(preg_match("/^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\.[a-zA-Z]{2,}$/", $email) !== 1) {
            return self::_InvalidFormat($res);
        }
        
        $salt = \utils::RandomString(64);
        $hash = hash("sha256", $salt.$password.$salt);
        $stmt = \app::Connection()->prepare(self::$userInsert);
        if(!$stmt->execute(["email" => $email, "salt" => $salt, "hash" => $hash])) {
            return self::_BadRequest($res);
        }
        $userID = \app::Connection()->lastInsertID();
        $token = self::RegisterToken($device, $userID);
        
        $res->SetBody(["token" => $token]);
        $res->SetStatusCode(200);
    }
    
    /**
     * Returns wether a request is properly authenticated.
     */
    public static function ValidateKey($req, $res) {
        $token = $req->Header("Authorization");
        $device = $req->Header("X-NetNotes-DeviceID");
        if(is_null($token) || is_null($device)) {
            return self::_Unauthorized($res);
        }
        $parts = explode(":", base64_decode($token));
        if(count($parts) !== 2) {
            return self::_Unauthorized($res);
        }
        list($email, $signature) = $parts;
        
        $stmt = \app::Connection()->prepare("SELECT * FROM `user` WHERE email = :email");
        $stmt->execute(["email" => $email]);
        $user = $stmt->fetch(\PDO::FETCH_ASSOC);
        if(!$user) {
            return self::_Unauthorized($res);
        }
        $token = self::GetToken($user["uniqueID"], $device);
        if(!$token) {
            return self::_Unauthorized($res);
        }
        
        $apiKey = \app::GetOption("auth.key");
        $serverHash = base64_encode(hash_hmac("sha256", $token, $apiKey, true));
        
        if($signature !== $serverHash) {
            return self::_Unauthorized($res);
        }
        \app::SetUserID($user["uniqueID"]);
        \app::SetDeviceID($device);
        return true;
    }
    
    /**
     * Returns wether a login request is valid, and if yes creates a new token for the given
     * user and device.
     */
    public static function ValidatePassword($req, $res) {
        
        $device = $req->Header("X-NetNotes-DeviceID");
        $email = $req->BasicUser();
        $password = $req->BasicPassword();
        if(is_null($device) || is_null($email) || is_null($password)) {
            return self::_Unauthorized($res);
        }
        
        $stmt = \app::Connection()->prepare("SELECT * FROM `user` WHERE email = :email");
        $stmt->execute(["email" => $email]);
        $user = $stmt->fetch(\PDO::FETCH_ASSOC);
        if(!$user) {
            return self::_Unauthorized($res);
        }
        
        $hash = hash("sha256", $user["salt"].$password.$user["salt"]);
        if($hash !== $user["hash"]) {
            return self::_Unauthorized($res);
        }
        
        \app::SetUserID($user["uniqueID"]);
        \app::SetDeviceID($device);
        return self::RegisterToken($device, $user["uniqueID"]);
    }
    
    /**
     * Registers a device for a user.
     */
    private static function RegisterToken($deviceID, $userID) {
        $token = \Utils::RandomString(128);
        $sql = "REPLACE INTO `token` (`userID`, `deviceID`, `token`, `pushToken`)
                VALUES (:userID, :deviceID, :token, :pushToken);";
        $stmt = \app::Connection()->prepare($sql);
        $result = $stmt->execute([
            "userID" => $userID,
            "deviceID" => $deviceID,
            "token" => $token,
            "pushToken" => null
        ]);
        if(!$result) {
            throw new \Exception("Database Error");
        }
        return $token;
    }
    
    /**
     * Returns the registered token for a given device and user
     */
    private static function GetToken($userID, $deviceID) {
        $sql = "SELECT `token`, `pushToken` FROM `token`
                WHERE `userID` = :userID AND `deviceID` = :deviceID;";
        $stmt = \app::Connection()->prepare($sql);
        $stmt->execute(["userID" => $userID, "deviceID" => $deviceID]);
        $result = $stmt->fetch(\PDO::FETCH_ASSOC);
        if(!$result) {
            return false;
        }
        return $result["token"];
    }
    
    /**
     * Sends a 401 Unauthorized response.
     */
    public static function _Unauthorized($res) {
        $res->SetBody(["fail" => "Unauthorized"]);
        $res->SetStatusCode(401);
        return false;
    }
    
    /**
     * Sends a 422 Unprocessable Entity response.
     */
    private static function _InvalidFormat($res) {
        $res->SetStatusCode(422);
        $res->SetBody(["fail" => "Invalid Entity."]);
        return false;
    }
    
    /**
     * Sends a 409 Conflict response.
     */
    public static function _BadRequest($res) {
        $res->SetBody(["fail" => "Bad Request"]);
        $res->SetStatusCode(400);
        return false;
    }
}