<?php
require_once 'DB.php';

$MESSAGE_STATUS_ALL = 0;
$MESSAGE_UNREAD = 1;
$MESSAGE_READ = 2;

function message_recieved($msgids) {
	$db = new DB();
	$pdo = $db->get_connection();
	$sql = 'update IBE_MESSAGE set STATUS = 2 where MESSAGE_ID in (';
	foreach ($msgids as $msgid) {
		$sql .= sprintf('%d,', $msgid);
	}
	$sql = substr($sql, 0, strlen($sql) - 1);
	$sql .= ')';
	$stmt = $pdo->prepare($sql);
	$stmt->execute();
	$pdo = null;
}

function add_message($from, $to, $content) {
	$db = new DB();
	$pdo = $db->get_connection();
	$sql = 'insert into IBE_MESSAGE values(null, :from, :to, :content, now(), 1)';
	$stmt = $pdo->prepare($sql);
	$stmt->bindValue(':from', $from);
	$stmt->bindValue(':to', $to);
	$stmt->bindValue(':content', $content, PDO::PARAM_LOB);
	$stmt->execute();
	$pdo = null;
}

function list_messages($user, $start_date, $end_date, $amount, $status) {
	$db = new DB();
	$pdo = $db->get_connection();
	$sqlf = 'select MESSAGE_ID, EMAIL, CONTENT, MESSAGE_DATE from IBE_MESSAGE m, IBE_USER u where m.FROM_USER=u.USER_ID and TO_ID=\'%s\' and MESSAGE_DATE>\'%s\' and MESSAGE_DATE<\'%s\'';
	if ($status > 0)
		$sqlf .= ' and STATUS=%d';
	$sql = '';
	if ($status > 0) {
		$sql = sprintf($sqlf, $user, $start_date, $end_date, $status);
	} else {
		$sql = sprintf($sqlf, $user, $start_date, $end_date);
	}
	$limitf = ' limit %d';
	$limit = sprintf($limitf, $amount);
	$sql .= $limit;
	$msgs = array();
	$i = 0;
	foreach ($pdo->query($sql) as $row) {
		$msg = array();
		$msg['MESSAGE_ID'] = $row['MESSAGE_ID'];
		$msg['FROM_USER'] = $row['EMAIL'];
		$msg['CONTENT'] = $row['CONTENT'];
		$msg['MESSAGE_DATE'] = $row['MESSAGE_DATE'];
		$msgs[$i] = $msg;
		$i++;
	}
	$pdo = null;
	return $msgs;
}
?>
