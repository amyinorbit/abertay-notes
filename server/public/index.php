<?php
/**
 * Simple server script for a single-user note taking application
 * Calls function from Controller.php
 */
namespace CesarParent;
require_once(__DIR__."/../REST/Engine.php");
use CesarParent\REST;

$app = new REST\Engine(__DIR__."/../config.json");

$app->route('GET', '/', function() use($app) {
    return REST\Response::success(200);
});


$app->execute($_REQUEST['resource']);