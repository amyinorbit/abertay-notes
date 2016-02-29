<?php
/**
 * utils.php - Synchronisation engine utility function.
 * 
 * @version     1.0a1
 * @author      Cesar Parent <cesar@cesarparent.com>
 */

class utils {
    
    /**
     * Returns a V4 UUID string.
     */
    public static function uuid() {
        return sprintf( '%04x%04x-%04x-%04x-%04x-%04x%04x%04x',
            mt_rand( 0, 0xffff ), mt_rand( 0, 0xffff ),
            mt_rand( 0, 0xffff ),
            mt_rand( 0, 0x0fff ) | 0x4000,
            mt_rand( 0, 0x3fff ) | 0x8000,
            mt_rand( 0, 0xffff ), mt_rand( 0, 0xffff ), mt_rand( 0, 0xffff )
        );
    }
    
    /**
     * Returns a random alphanumeric string of a given length.
     */
    public static function randomString($length) {
        $valid_chars = "012345678ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz";
        $str = "";
        foreach(range(0, $length-1) as $i) {
            $str .= $valid_chars[rand(0, strlen($valid_chars) - 1)];
        }
        return $str;
    }
    
}
