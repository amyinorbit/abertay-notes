<?php

namespace controllers;
class debug {
    
    private static $header = <<<EOT
    <!DOCTYPE html>
    <html lang="en">
    <head>
    <meta charset="UTF-8">
    <style type="text/css">
    body {
        width: 90%;
        max-width: 1024px;
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
    
    table {
        border-collapse: collapse;
    }
    
    table, th, td {
        vertical-align: top;
        text-align: left;
    }
    
    td, th {
        padding: .5em;
    }
    
    #notes tr:first-child {
        margin-bottom: .2em;
        border-bottom: 2px dotted black; 
    }
    </style>
    </head>
    <body>
EOT;

    private static $footer = <<<EOT
    </body>
    </html>
EOT;

    public static function Display($req, $res, $email) {
        
        self::Start();
        
        $user = self::GetUser($email);
        if(is_null($user)) {
            return self::Finish($res);
        }
        self::PrintUser($user);
        self::PrintNotes($user["uniqueID"]);
        
        self::Finish($res);
    }
    
    public static function PrintUser($user) {
        echo "<section id=\"user\">\n";
        echo "<h1>".$user["email"]." (User #".$user["uniqueID"].")</h1>\n";
        echo "<table>\n";
        echo "<tr><td>Update Transaction ID</td><td>".$user["updateSeqID"]."</td></tr>\n";
        echo "<tr><td>Delete Transaction ID</td><td>".$user["deleteSeqID"]."</td></tr>\n";
        echo "</table>\n";
        echo "</section>\n";
    }
    
    public static function PrintNotes($userID) {
        
        $stmt = \app::Connection()->prepare("SELECT * FROM `note` WHERE userID = :userID");
        if(!$stmt->execute(["userID" => $userID])) {
            return;
        }
        
        echo "<section id=\"notes\">\n";
        echo "<h1>".$user["email"]."</h1>\n";
        echo "<table>\n";
        echo "<tr><th>ID</th><th>Contents</th><th>T.ID</th></tr>\n";
        while($note = $stmt->fetch(\PDO::FETCH_ASSOC)) {
            echo "<tr>\n";
            echo "<td>".$note["uniqueID"]."</td>";
            echo "<td>".$note["text"]."</td>";
            echo "<td>".$note["seqID"]."</td>";
            echo "</tr>\n";
        }
        echo "</table>\n";
        echo "</section>\n";
    }
    
    public static function Start() {
        ob_start();
        echo self::$header;
    }
    
    public static function Finish($res) {
        echo self::$footer;
        $out = ob_get_clean();
        $res->SetBody($out);
        $res->SetHeader("Content-Type", "text/html");
    }
    
    private static function GetUser($email) {
        $stmt = \app::Connection()->prepare("SELECT * FROM `user` WHERE email = :email");
        $stmt->execute(["email" => $email]);
        $user = $stmt->fetch(\PDO::FETCH_ASSOC);
        return ($user !== false) ? $user : null;
    }
}