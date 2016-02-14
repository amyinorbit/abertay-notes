<?php
spl_autoload_register(function($className) {
    $class = trim($className, "\\");
    require_once(__DIR__."/".str_replace("\\", "/", $class).".php");
});