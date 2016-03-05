<?php

namespace controllers;
class auth {
    
    /**
     * Returns wether a request is properly authenticated
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
        return true;
    }
    
    /**
     * Returns wether a login request is valid, and if yes creates a new token for the given
     * user and device.
     */
    public static function ValidatePassword($req, $res) {
        
        $device = $req->Header("X-NetNotes-DeviceID");
        $token = $req->Header("Authorization");
        if(is_null($token) || is_null($device)) {
            return self::_Unauthorized($res);
        }
        if(strpos(strtolower($token), "basic") !== 0) {
            return self::_Unauthorized($res);
        }
        $token = base64_decode(substr($token, 6));
        list($userID, $password) = explode(":", $token);
        
        $stmt = \app::Connection()->prepare("SELECT * FROM `user` WHERE email = :email");
        $stmt->execute(["email" => $userID]);
        $user = $stmt->fetch(\PDO::FETCH_ASSOC);
        if(!$user) {
            return self::_Unauthorized($res);
        }
        
        $hash = hash("sha256", $user["salt"].$password.$user["salt"]);
        if($hash !== $user["hash"]) {
            return self::_Unauthorized($res);
        }
        
        \app::SetUserID($user["uniqueID"]);
        return self::RegisterToken($device, $user["uniqueID"], self::ParsePushToken($req));
    }
    
    /**
     * Registers 
     *
     */
    private static function RegisterToken($deviceID, $userID, $pushToken = null) {
        $token = \Utils::RandomString(128);
        $sql = "REPLACE INTO `token` (`userID`, `deviceID`, `token`, `pushToken`)
                VALUES (:userID, :deviceID, :token, :pushToken);";
        $stmt = \app::Connection()->prepare($sql);
        $result = $stmt->execute([
            "userID" => $userID,
            "deviceID" => $deviceID,
            "token" => $token,
            "pushToken" => $pushToken
        ]);
        if(!$result) {
            throw new \Exception("Database Error");
        }
        return $token;
    }
    
    /**
     * Returns the push token sent in a login request, or null if there isn't a valid one.
     */
    private static function ParsePushToken($request) {
        $json = json_decode($request->Body(), true);
        if(is_null($json)) { return null; }
        if(!isset($json["pushToken"])) { return null; }
        if(!is_string($json["pushToken"])) { return null; }
        return $json["pushToken"];
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
        if(!is_null($result["pushToken"])) {
            \app::setPushToken($result["pushToken"]);
        }
        return $result["token"];
    }
    
    /**
     * Send a 401 Unauthorized response
     */
    public static function _Unauthorized($res) {
        $res->SetBody(["fail" => "Unauthorized"]);
        $res->SetStatusCode(401);
        return false;
    }
}