<?php
/**
 * Simple server script for a single-user note taking application
 * Calls function from Controller.php
 */
namespace CesarParent;
require_once(__DIR__."/../REST/Engine.php");
require_once(__DIR__."/../ViewModel.php");
use CesarParent\REST;
use CesarParent\ViewModel;

$app = new REST\Engine(__DIR__."/../config.json");
$vm = new ViewModel\ViewModel($app);

$app->route('GET', '/', function() use($app) {
    return REST\Response::success(200);
});

$app->route('GET', '/notes', function() use($vm) {
    return $vm->get_notes();
});

$app->route('POST', '/notes', function() use($vm) {
    return $vm->create_notes();
});

$app->route('GET', '/notes/([0-9]+)', function($id) use($vm) {
    return $vm->get_note($id);
});

$app->route('PUT', '/notes/([0-9]+)', function($id) use($vm) {
    return $vm->edit_note($id);
});

$app->route('DELETE', '/notes/([0-9]+)', function($id) use($vm) {
    if(!$vm->authenticate()) {
        return REST\Response::fail(401);
    }
    return $vm->delete_note($id);
});

$app->execute($_REQUEST['resource']);