<?php
/**
 * Simple server script for a single-user note taking application
 * Calls functions from Controller.php
 */
require_once(__DIR__."/../loader.php");

$router = new http\router;

$router->onGet("/", function($req, $res) {
    $res->setHeader("Content-Type", "text/plain");
    $res->send("hello!\n");
});


(new http\server(function($req, $res) use($router) {
    try {
        $router->dispatch($req, $res);
    }
    catch(\Exception $e) {
        $res->setStatusCode(500);
        $res->setHeader("Content-Type", "text/plain");
        $res->setBody($e->getMessage);
    }
}))->start();
