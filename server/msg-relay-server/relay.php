<?php
require_once 'UserDAO.php';
require_once 'MessageDAO.php';

$action = $_REQUEST['action'];

if (!$action)
	return;
if (0 == strcasecmp('newmessage', $action)) {
	$email = $_REQUEST['email'];
	$password = $_REQUEST['password'];
	$user_id = check_user($email, $password);
	if (!$user_id) {
		echo '1';
		return;
	}
	$content = $_REQUEST['content'];
	$recipient = $_REQUEST['recipient'];
	add_message($user_id, $recipient, $content);
	echo '0';
	return;
}
if (0 == strcasecmp('getmessage', $action)) {
	$email = $_REQUEST['email'];
	$password = $_REQUEST['password'];
	$user_id = check_user($email, $password);
	if (!$user_id) {
		echo '1';
		return;
	}
	$start_date = $_REQUEST['start'];
	$end_date = $_REQUEST['end'];
	$amount = $_REQUEST['amount'];
	$status = $_REQUEST['status'];
	$received_messages = $_REQUEST['rmessages'];
	date_default_timezone_set('Asia/Shanghai');
	if (!$start_date) {
		$start_date = '1970-00-00 00:00:00';
	} else {
		$dt = new DateTime();
		$dt->setTimestamp($start_date);
		$start_date = $dt->format('Y-m-d H:i:s');
	}
	if (!$end_date) {
		$now = date_create();
		$end_date = date_format($now, 'Y-m-d H:i:s');
	} else {
		$dt = new DateTime();
		$dt->setTimestamp($end_date);
		$end_date = $dt->format('Y-m-d H:i:s');
	}
	if ($status != 1 && $status != 2) {
		$status = 0;
	}
	if (!$amount) {
		$amount = 10;
	}
	if ($received_messages) {
		$msg_array = explode(',', $received_messages);
		message_recieved($msg_array);
	}
	$sleep_time = 1000;
	$limit0 = 60000;
	$limit1 = 600000;
	$limit2 = 3600000;
	for (;;) {
		$messages = list_messages($email, $start_date, $end_date, $amount, $status);
		if (count($messages) > 0) {
			echo json_encode($messages);
			flush();
			return;
		}
		usleep($sleep_time);

		$now = date_create();
		$end_date = date_format($now, 'Y-m-d H:i:s');

		if ($sleep_time < $limit0)
			$sleep_time += 1000;
		else if ($sleep_time < $limit1)
			$sleep_time += 5000;
		else if ($sleep_time < $limit2)
			$sleep_time += 50000;
		else {
			echo json_encode(array('control' => 'nomessage'));
			flush();
			return;
		}
	}
}
?>
