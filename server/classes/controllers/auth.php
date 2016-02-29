<?php

namespace controllers;
class auth {
    
    public static function Validate($req, $res) {
        
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
        
        \app::SetUserID($user["uniqueID"]);
        $hash = hash("sha256", $user["salt"].$password.$user["salt"]);
        
        if($hash !== $user["hash"]) {
            return self::_Unauthorized($res);
        }
        return true;
    }
    
    public static function _Unauthorized($res) {
        $res->SetBody(["fail" => "Unauthorized"]);
        $res->SetStatusCode(401);
        return false;
    }
    
}