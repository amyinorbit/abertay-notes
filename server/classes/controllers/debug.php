<?php

namespace controllers;
class debug {
    
    private static $header = <<<EOT
    <!DOCTYPE html>
    <html>
    <header>
    <style type="text/css">
    body {
        width: 90%;
        max-width: 750px;
        margin: 0 auto;
        padding: 2em 5%;
        color: black;
        background: white;
    }
    
    * {
        font-size: 12pt;
        color: black;
        font-family: menlo, courrier, monospace;
    }
    </style>
    </header>
    <body>
EOT;

    private static $footer = <<<EOT
    </body>
    </html>
EOT;

    public static function Display($req, $res, $email) {
        
        
        
        ob_start();
        echo self::$header;
        echo "Hello";
        echo self::$footer;
        
        $user = self::GetUser($email);
        if(is_null($user)) {
            return self::Finish($res);
        }
        self::PrintUser($user);
        
        self::Finish($res);
    }
    
    public static function PrintUser($user) {
        echo "<section id=\"user\">\n";
        echo "<h1>".$user["email"]."</h1>\n";
        echo "<table>\n";
        echo "<tr><td>Update Transaction ID</td><td>".$user["updateSeqID"]."</td></tr>\n";
        echo "<tr><td>Delete Transaction ID</td><td>".$user["deleteSeqID"]."</td></tr>\n";
        echo "</section>\n";
    }
    
    public static function PrintNotes($userID) {
        
    }
    
    public static function Finish($res) {
        $res->SetHeader("Content-Type", "text/html");
        $out = ob_get_clean();
        $res->SetBody($out);
    }
    
    private static function GetUser($email) {
        $stmt = \app::Connection()->prepare("SELECT * FROM `user` WHERE email = :email");
        $stmt->execute(["email" => $email]);
        $user = $stmt->fetch(\PDO::FETCH_ASSOC);
        return ($user !== false) ? $user : null;
    }
}