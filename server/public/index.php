<?php
/**
 * Simple server script for a single-user note taking application
 * Calls functions from Controller.php
 */
require_once(__DIR__."/../loader.php");
date_default_timezone_set("Europe/London");

$router = new http\router;
$auth = new controllers\auth;
$sync = new controllers\sync;

$router->OnPost("/notes", function($req, $res) {
    
});

$router->OnPost("/deleted", function($req, $res) {
    
});

$router->OnGet("/", function($req, $res) {
    $res->SetBody(["message" => "Installation Successful"]);
});


$server = new http\server(function($req, $res) use($router) {
    try {
        $router->Dispatch($req, $res);
    }
    catch(\Exception $e) {
        $res->SetStatusCode(500);
        $res->SetBody(["error" => $e->getMessage()]);
    }
});

// Encode every response as JSON
$server->FilterOut(function($res) {
    $res->SetHeader("Content-Type", "application/json");
    $res->SetHeader("X-NetNotes-Time", strval(time()));
    $res->SetBody(json_encode($res->Body(), JSON_PRETTY_PRINT)."\n");
});

$server->Start();
