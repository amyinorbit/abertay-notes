<?php
/**
 * app.php - App holds the server's options and the database connection
 * 
 * @version     1.0a1
 * @author      Cesar Parent <cesar@cesarparent.com>
 * @package     notes
 */

class app {
    
    private static $userID = null;
    private static $conn = null;
    private static $options = [
        "app.timezone" =>   "UTC",
        "app.url" =>        "localhost",
    ];
    
    /**
     * Initialises the application with a JSON configuration file.
     */
    public static function Init($config) {
        if(!file_exists($config)) {
            throw new \Exception("No such file: ".$config);
        }
        $json = file_get_contents($config);
        $options = json_decode($json, true);
        if(is_null($options)) {
            throw new \Exception("Invalid Config File: ".$config);
        }
        self::$options = array_merge(self::$options, $options);
        
        // Set the timezone and open a database connection
        date_default_timezone_set(self::$options["app.timezone"]);
        self::$conn = new \PDO("sqlite:".dirname($config)."/".self::$options["db.name"]);
    }
    
    /**
     * Returns the value matching $key in the app option's.
     */
    public static function GetOption($key, $value = null) {
        if(!isset(self::$options[$key])) {
            return $value;
        }
        return self::$options[$key];
    }
    
    public static function SetUserID($userID) {
        self::$userID = $userID;
    }
    
    public static function UserID() {
        return self::$userID;
    }
    
    /**
     * Returns the database connection handle.
     */
    public static function Connection() {
        return self::$conn;
    }
    
}