<?php
spl_autoload_register(function($className) {
    $class = strtolower(trim($className, "\\"));
    require_once(__DIR__."/".str_replace("\\", "/", $class).".php");
});