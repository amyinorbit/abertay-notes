<?php
/**
 * app.php - App holds the server's options and the database connection
 * 
 * @version     1.0a1
 * @author      Cesar Parent <cesar@cesarparent.com>
 * @package     notes
 */

class app {
    
    private static $deviceID = null;
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
        $pdo = "mysql:host=".self::$options["db.server"].";dbname=".self::$options["db.name"];
        self::$conn = new \PDO($pdo,
                               self::$options["db.user"],
                               self::$options["db.password"]);
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
    
    /**
     * Registers the current user's ID with the app.
     */
    public static function SetUserID($userID) {
        self::$userID = $userID;
    }
    
    /**
     * Registers a device ID token with the app.
     */
    public static function SetDeviceID($id) {
        self::$deviceID = $id;
    }
    
    /**
     * Returns the current user's ID if there is one registered, or null
     */
    public static function UserID() {
        return self::$userID;
    }
    
    /**
     * Returns the current device's ID if there is one registered, or null
     */
    public static function DeviceID() {
        return self::$deviceID;
    }
    
    /**
     * Returns the database connection handle.
     */
    public static function Connection() {
        return self::$conn;
    }
    
}