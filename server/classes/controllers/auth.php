<?php

namespace controllers;
class auth {
    
    public static function GetToken($req, $res) {
        
    }
    
    public static function ValidateKey($req, $res) {
        $token = $req->Header("Authorization");
        if(is_null($token)) {
            return self::_Unauthorized($res);
        }
        $parts = explode(":", base64_decode($token));
        if(count($parts) !== 2) {
            return self::_Unauthorized($res);
        }
        list($email, $token) = $parts;
        
        $stmt = \app::Connection()->prepare("SELECT * FROM `user` WHERE email = :email");
        $stmt->execute(["email" => $userID]);
        $user = $stmt->fetch(\PDO::FETCH_ASSOC);
        if(!$user) {
            return self::_Unauthorized($res);
        }
        
        $apiKey = \app::GetOption("app.key");
        $userHash = hash_hmac("sha256", $token, $apiKey);
        $servHash = hash_hmac("sha256", $user->token, $apiKey);
        
        if($userHash !== $servHash) {
            return self::_Unauthorized($res);
        }
        \app::SetUserID($user["uniqueID"]);
        return true;
    }
    
    public static function ValidatePassword($req, $res) {
        
        $token = $req->Header("Authorization");
        if(is_null($token)) {
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
        $token = "";
        if(is_null($user["token"])) {
            $token = \utils::RandomString(128);
            $sql = "UPDATE `user` SET `token` = :token WHERE `uniqueID` = :uniqueID;";
            $stmt = \app::Connection()->prepare($sql);
            $stmt->execute(["token" => $token, "uniqueID" => $user["uniqueID"]]);
        } else {
            $token = $user["token"];
        }
        return $token;
    }
    
    public static function _Unauthorized($res) {
        $res->SetBody(["fail" => "Unauthorized"]);
        $res->SetStatusCode(401);
        return false;
    }
}