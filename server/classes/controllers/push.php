<?php

namespace controllers;
class push {
    
    private static $update = <<<EOT
    UPDATE `token` SET `pushToken` = :token
    WHERE `userID` = :userID AND `deviceID` = :deviceID;
EOT;
    
    public static function RegisterToken($req, $res) {
        $json = json_decode($request->Body(), true);
        if(is_null($json) || !isset($json["token"]) || !is_string($json["token"])) {
            return self::_InvalidFormat($res);
        }
        $token = $json["token"];
        $db = \app::Connection();
        $db->beginTransaction();
        $stmt = $db->prepare(self::$update);
        if(!$stmt->execute([
            "token" => $token,
            "userID" => \app::UserID(),
            "deviceID" => \app::DeviceID()
        ])) {
            throw new \Exception("Database Error");
        }
        $db->commit();
        $res->SetStatus(200);
        $res->SetBody([]);
    }
    
    public static function PushToDevices($action) {
        $tokens = self::GetTokens(\app::UserID(), \app::DeviceID());
        if(count($tokens) === 0) { return; }
        
        $url = \app::GetOption("gcm.server", "http://localhost");
        $apiKey = \app::GetOption("gcm.key", "");
        
        $headers = [
            "Authorization: key=".$apiKey,
            "Content-Type: application/json"
        ];
        
        $payload = [
            "registration_ids" => $tokens,
            "data" => [ "action" => $resource ]
        ];
        
        $ch = curl_init();
        curl_setopt($ch, CURLOPT_URL, $url);      
        curl_setopt($ch, CURLOPT_POST, true);
        curl_setopt($ch, CURLOPT_HTTPHEADER, $headers);
        curl_setopt($ch, CURLOPT_RETURNTRANSFER, true);
        curl_setopt($ch, CURLOPT_POSTFIELDS, json_encode($payload));
        $result = curl_exec($ch);
        if (curl_errno($ch)) {
            throw new \Exception("GCM error: ".curl_error($ch));
        }
        curl_close( $ch );
    }
    
    private static function GetTokens($userID, $deviceID) {
        $sql = "SELECT `pushToken` FROM `token`
                WHERE `userID` = :userID AND `deviceID` <> :deviceID AND `pushToken` IS NOT NULL;";
        $stmt = \app::Connection()->prepare($sql);
        $stmt->execute(["userID" => $userID, "deviceID" => $deviceID]);
        $tokens = [];
        while($token = $stmt->fetch(\PDO::FETCH_ASSOC)) {
            array_push($tokens, $token["pushToken"]);
        }
        return $tokens;
    }
    
    private static function _InvalidFormat($res) {
        $res->SetStatusCode(422);
        $res->SetBody(["fail" => "Invalid Entity."]);
    }
}