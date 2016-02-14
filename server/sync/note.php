<?php
/**
 * note.php - Note representation
 * 
 * @version     1.0a1
 * @author      Cesar Parent <cesar@cesarparent.com>
 * @package     sync
 */
namespace sync;

class note {
    public $content;
    public $uuid;
    public $create_date;
    public $mod_date;
    
    public static function from_array(array $data) {
        $note = new note;
        foreach($data as $k => $v) {
            $note->{$k} = $v;
        }
        return $note;
    }
}