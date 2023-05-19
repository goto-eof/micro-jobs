package com.andreidodu.constants;

public interface JobInstanceConst {
    final static int STATUS_CREATED = 0;
    final static int STATUS_WORKER_WORK_REQUEST = 10;
    final static int STATUS_WORK_PROVIDER_WORK_REQUEST_ACCEPT = 20;
    final static int STATUS_WORKER_WORK_START = 30;
    final static int STATUS_WORKER_WORK_END = 40;
    final static int STATUS_WORK_PROVIDER_WORK_RECEIVED = 50;
    final static int STATUS_WORK_PROVIDER_WORK_APPROVE = 60;
    final static int STATUS_WORK_PROVIDER_PAYMENT = 70;
    final static int STATUS_WORKER_RECEIVED_PAYMENT = 80;
    final static int STATUS_PROCESS_COMPLETE = 90;

}
