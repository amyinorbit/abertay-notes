<?php
/**
 * Simple server script for a single-user note taking application
 * Calls functions from Controller.php
 */

require_once(__DIR__."/../loader.php");

error_reporting(E_ALL);

(new http\server(function($req, $res) {
    $res->setHeader("Content-Type", "text/plain");
    $res->setStatusCode(200);
    $res->send($req->query("test", -1)."\n");
    $res->send("Hello\n");
}))->start();