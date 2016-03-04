<?php
/**
 * Simple server script for a single-user note taking application
 * Calls functions from Controller.php
 */
require_once(__DIR__."/../loader.php");

error_reporting(E_ALL);

$router = new http\router;
$auth = new controllers\auth;

$router->OnPost("/notes", function($req, $res) {
    if(!controllers\auth::ValidateKey($req, $res)) { return; }
    (new controllers\sync)->Update($req, $res);
});

$router->OnPost("/deleted", function($req, $res) {
    if(!controllers\auth::ValidateKey($req, $res)) { return; }
    (new controllers\sync)->Delete($req, $res);
});

$router->OnGet("/", function($req, $res) {
    $res->SetBody([
        "message" => "Installation Successful",
        "app" => [
            "version" => \app::GetOption("app.version", "1.0.0a"),
            "url" => \app::GetOption("app.url", "")
        ]
    ]);
    
});

$router->OnGet("/login", function($req, $res) {
    if(!controllers\auth::ValidateKey($req, $res)) { return; }
    $res->SetStatusCode(200);
    $res->SetBody(["message" => "Token Authentication Successful"]);
});

$router->OnPost("/login", function($req, $res) {
    $token = controllers\auth::ValidatePassword($req, $res);
    if($token === false) {
        $res->SetStatusCode(401);
        $res->SetBody(["error" => "Invalid username or password."]);
    } else {
        $res->SetStatusCode(200);
        $res->SetBody(["token" => $token]);
    }
});


$server = new http\server(function($req, $res) use($router) {
    try {
        \app::Init(__DIR__."/../config.json");
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
    $res->SetHeader("X-NetNotes-Time", (new DateTime())->format("Y-m-d\TH:i:sO"));
    $res->SetBody(json_encode($res->Body(), JSON_UNESCAPED_SLASHES));
});

$server->Start();
