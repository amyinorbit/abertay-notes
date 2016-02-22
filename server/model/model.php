<?php

namespace model;
class app {
    
    private static $conn;
    private static $options;
    
    public static function Init($config) {
        if(!file_exists($config)) {
            throw new \Exception("No such file: ".$config);
        }
        $json = file_get_contents($config);
        $options = json_decode($json);
        if(is_null($json)) {
            throw new \Exception("Invalid Config File: ".$config);
        }
        self::$options = $options;
    }
    
    public static function GetOption($key, $value = null) {
        if(!isset(self::$options[$key])) {
            return $value;
        }
        return self::$options[$key];
    }
    
    public static function Connection() {
        return self::$conn;
    }
    
}