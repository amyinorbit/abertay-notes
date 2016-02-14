<?php
/**
 * Simple server script for a single-user note taking application
 * Calls functions from Controller.php
 */
require_once(__DIR__."/../loader.php");

$router = new http\router;

$router->onPost("/", function($req, $res) {
    $engine = new sync\engine(__DIR__."/../config.json");
    $engine->processRequest($req, $res);
    $res->setHeader("Content-Type", "application/json");
    $res->setBody(json_encode($res->body(), JSON_PRETTY_PRINT)."\n");
});


(new http\server(function($req, $res) use($router) {
    try {
        $router->dispatch($req, $res);
    }
    catch(\Exception $e) {
        $res->setStatusCode(500);
        $res->setHeader("Content-Type", "text/plain");
        $res->setBody($e->getMessage());
    }
}))->start();
