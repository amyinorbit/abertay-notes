<?php

$headers = getallheaders();

ob_start();

echo $_SERVER["REQUEST_METHOD"]."\n";

foreach($headers as $k => $v) {
    echo $k.": ".$v."\n";
}

echo "\n".file_get_contents("php://input");

$out = ob_get_clean();

mkdir(__DIR__."/out");
file_put_contents(__DIR__."/out/".time().".txt", $out);

