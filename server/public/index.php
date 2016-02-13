<?php
/**
 * Simple server script for a single-user note taking application
 * Calls function from Controller.php
 */

require_once(__DIR__."/../http/server.php");

error_reporting(E_ALL);

(new http\server(function($req, $res) {
    $res->setHeader("Content-Type", "text/plain");
    $res->setStatusCode(404);
    $res->send("Hello\n");
}))->start();