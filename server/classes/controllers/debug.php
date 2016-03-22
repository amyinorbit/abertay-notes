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
        color: white;
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

    public static function Display($req, $res, $id) {
        
        ob_start();
        echo self::$header;
        echo "Hello";
        echo self::$footer;
        $out = ob_get_clean();
        
        $res->SetHeader("Content-Type", "text/html");
        $res->SetBody($out);
    }
    
}