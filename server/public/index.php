<?php
/**
 * Simple server script for a single-user note taking application
 * Calls functions from Controller.php
 */
require_once(__DIR__."/../loader.php");

error_reporting(0);

$router = new http\router;
$auth = new controllers\auth;

$router->OnPost("/notes", function($req, $res) {
    if(!controllers\auth::ValidateKey($req, $res)) { return; }
    if(!controllers\sync::Update($req, $res)) { return; }
    controllers\push::PushToDevices("update");
});

$router->OnPost("/deleted", function($req, $res) {
    if(!controllers\auth::ValidateKey($req, $res)) { return; }
    if(!controllers\sync::Delete($req, $res)) { return; }
    controllers\push::PushToDevices("delete");
});

$router->OnPost("/token", function($req, $res) {
    if(!controllers\auth::ValidateKey($req, $res)) { return; }
    controllers\push::RegisterToken($req, $res);
});

$router->OnPost("/signup", function($req, $res) {
    controllers\auth::AddUser($req, $res);
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
    $res->SetHeader("X-NetNotes-Time", strval(time()));
    $res->SetBody(json_encode($res->Body(), JSON_UNESCAPED_SLASHES));
});

$server->Start();
