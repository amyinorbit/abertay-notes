<?php
/**
 * Basic note model
 */
namespace CesarParent\Model;

class Note {
    public $title;
    public $content;
    public $uuid;
    public $create_date;
    public $mod_date;
    private $deleted;
    
    public static function from_array(array $data) {
        $note = new Note;
        foreach($data as $k => $v) {
            $note->{$k} = $v;
        }
        $note->title = substr($note->content, 0, 127);
        return $note;
    }
}