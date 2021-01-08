-- Copyright 2018-2020 Elimu Informatics
--
-- Licensed under the Apache License, Version 2.0 (the "License");
-- you may not use this file except in compliance with the License.
-- You may obtain a copy of the License at
--
--     http://www.apache.org/licenses/LICENSE-2.0
--
-- Unless required by applicable law or agreed to in writing, software
-- distributed under the License is distributed on an "AS IS" BASIS,
-- WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
-- See the License for the specific language governing permissions and
-- limitations under the License.

-- MySQL dump 10.13  Distrib 5.7.23, for Linux (x86_64)
--
-- Host: localhost    Database: a2d2
-- ------------------------------------------------------
-- Server version	5.7.23-0ubuntu0.16.04.1

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!40101 SET NAMES utf8 */;
/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;

--
-- Table structure for table `CDSHook`
--

DROP TABLE IF EXISTS `CDSHook`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `CDSHook` (
  `id` int(11) NOT NULL,
  `name` longtext,
  `description` longtext,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `CDSHook`
--

LOCK TABLES `CDSHook` WRITE;
/*!40000 ALTER TABLE `CDSHook` DISABLE KEYS */;
/*!40000 ALTER TABLE `CDSHook` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `serviceinfo`
--

DROP TABLE IF EXISTS `serviceinfo`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `serviceinfo` (
  `id` varchar(255) NOT NULL,
  `version` int(11) NOT NULL,
  `servicedata` longtext NOT NULL,
  `servicetype` varchar(20) NOT NULL,
  `defaultCustomer` character varying(255) default NULL,
  `timestamp` bigint default 1,
  `status` varchar(255) default 'done',
   PRIMARY KEY (`id`,`version`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `serviceinfo`
--

LOCK TABLES `serviceinfo` WRITE;
/*!40000 ALTER TABLE `serviceinfo` DISABLE KEYS */;
-- INSERT INTO `serviceinfo` VALUES ('create-resource',2,'io.elimu.a2d2:create-resource:0.0.1-SNAPSHOT','kie'),('pro-recommend',1,'io.elimu.a2d2:pro-recommend:0.0.1-SNAPSHOT','kie'),('pro-single',2,'org.easi-pro.order:pro-single:1.0.4','kie');
/*!40000 ALTER TABLE `serviceinfo` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `client`
--

DROP TABLE IF EXISTS `client`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `client` (
  `id` int(11) NOT NULL,
  `name` varchar(20) DEFAULT NULL,
  `type` longtext,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `client`
--

LOCK TABLES `client` WRITE;
/*!40000 ALTER TABLE `client` DISABLE KEYS */;
INSERT INTO `client` VALUES (1,'abc','Practitioner'),(2,'xyz','soft'),(123,'123','Practitioner'),(124,'xyz','Patient'),(125,'avc','Practitioner');
/*!40000 ALTER TABLE `client` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `client_program`
--

DROP TABLE IF EXISTS `client_program`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `client_program` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `cid` int(11) DEFAULT NULL,
  `pid` int(11) DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `client_program_ukey` (`cid`,`pid`),
  UNIQUE KEY `ukey` (`cid`,`pid`),
  KEY `pfk` (`pid`),
  CONSTRAINT `cfk` FOREIGN KEY (`cid`) REFERENCES `client` (`id`) ON DELETE NO ACTION ON UPDATE NO ACTION,
  CONSTRAINT `fk` FOREIGN KEY (`cid`) REFERENCES `client` (`id`) ON DELETE NO ACTION ON UPDATE NO ACTION,
  CONSTRAINT `fk2` FOREIGN KEY (`pid`) REFERENCES `program` (`id`) ON DELETE NO ACTION ON UPDATE NO ACTION,
  CONSTRAINT `pfk` FOREIGN KEY (`pid`) REFERENCES `program` (`id`) ON DELETE NO ACTION ON UPDATE NO ACTION
) ENGINE=InnoDB AUTO_INCREMENT=4 DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `client_program`
--

LOCK TABLES `client_program` WRITE;
/*!40000 ALTER TABLE `client_program` DISABLE KEYS */;
INSERT INTO `client_program` VALUES (1,1,1),(2,1,2),(3,125,1);
/*!40000 ALTER TABLE `client_program` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `program`
--

DROP TABLE IF EXISTS `program`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `program` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `name` varchar(20) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `program`
--

LOCK TABLES `program` WRITE;
/*!40000 ALTER TABLE `program` DISABLE KEYS */;
INSERT INTO `program` VALUES (1,'pgx'),(2,'abc');
/*!40000 ALTER TABLE `program` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `service_resources`
--

DROP TABLE IF EXISTS `service_resources`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `service_resources` (
  `servicename` longtext NOT NULL,
  `resourcefile` longtext,
  `resourcetype` longtext,
  `resourcetextcode` longtext,
  `id` int(11) NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `service_resources`
--

LOCK TABLES `service_resources` WRITE;
/*!40000 ALTER TABLE `service_resources` DISABLE KEYS */;
/*!40000 ALTER TABLE `service_resources` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `user_keys`
--

DROP TABLE IF EXISTS `user_keys`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `user_keys` (
  `userid` int(11) NOT NULL,
  `key` longtext,
  `jws_algorithm_type` longtext,
  `public_key` longtext,
  `private_key` longtext,
  `iss` longtext
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `user_keys`
--

LOCK TABLES `user_keys` WRITE;
/*!40000 ALTER TABLE `user_keys` DISABLE KEYS */;
INSERT INTO `user_keys` VALUES (1,'pHG5FtU0rL/mmaWZbCpbT3xQ499TUvOOyaTIE22L37I=',NULL,NULL,NULL,NULL),(2,NULL,'ES256','MFkwEwYHKoZIzj0CAQYIKoZIzj0DAQcDQgAEq8Ftr50G2k2OPdge05jBkc6gC12Fq3Y+HyT21lEOty6io2EbLcbIrayoJu4Wi1hA9OrosxrlSidDqhc64JNgMA==','MIGHAgEAMBMGByqGSM49AgEGCCqGSM49AwEHBG0wawIBAQQg6qtG04heSvc3DgC6\nnrtTiOtCyLXzAALhu7HUAwOiT6+hRANCAASrwW2vnQbaTY492B7TmMGRzqALXYWrdj4fJPbWUQ63LqKjYRstxsitrKgm7haLWED06uizGuVKJ0OqFzrgk2Aw','http://sandbox.cds-hooks.org'),(1,'pHG5FtU0rL/mmaWZbCpbT3xQ499TUvOOyaTIE22L37I=',NULL,NULL,NULL,NULL),(2,NULL,'ES256','MFkwEwYHKoZIzj0CAQYIKoZIzj0DAQcDQgAEq8Ftr50G2k2OPdge05jBkc6gC12Fq3Y+HyT21lEOty6io2EbLcbIrayoJu4Wi1hA9OrosxrlSidDqhc64JNgMA==','MIGHAgEAMBMGByqGSM49AgEGCCqGSM49AwEHBG0wawIBAQQg6qtG04heSvc3DgC6\nnrtTiOtCyLXzAALhu7HUAwOiT6+hRANCAASrwW2vnQbaTY492B7TmMGRzqALXYWrdj4fJPbWUQ63LqKjYRstxsitrKgm7haLWED06uizGuVKJ0OqFzrgk2Aw','http://sandbox.cds-hooks.org');
/*!40000 ALTER TABLE `user_keys` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `user_session`
--

DROP TABLE IF EXISTS `user_session`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `user_session` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `ipaddress` longtext,
  `expiretime` date DEFAULT NULL,
  `publickey` longblob,
  `privatekey` longblob,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=15 DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `user_session`
--

LOCK TABLES `user_session` WRITE;
/*!40000 ALTER TABLE `user_session` DISABLE KEYS */;
INSERT INTO `user_session` VALUES (14,'0:0:0:0:0:0:0:1','2017-12-25',_binary '0\\0\r	*�H��\r\0K\00HA\0\�q+�?\�\�@NI���(�O(� ��\�\�taU�]�P\����\�6��pJ� >B՝k#qo��$��BT_\0',_binary '0�U\00\r	*�H��\r\0�?0�;\0A\0\�q+�?\�\�@NI���(�O(� ��\�\�taU�]�P\����\�6��pJ� >B՝k#qo��$��BT_\0@d��>��^�Z\�a�(!|�8�-\�ԫ\�\�\rtb����ϖ�o�9�*)�U\�\�-\\�WV�e\�a!\0\�/�l.\��@ײ�\���\n[�zuN9����}�2f�%!\0ۻ�\0�&��,ar5�`\�\�B\�\n#�n- �\�23 ���=7tsjx�1��%ñU\r\������l{|�A�!\0\�`G�\�.3\��_�\�g\"\�\�w|\�h5�\�\�!\0�\'96��\r\�\0u#��C45#\r\�\�\�&�\�[��');
/*!40000 ALTER TABLE `user_session` ENABLE KEYS */;
UNLOCK TABLES;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;


-- Dump completed on 2018-08-28 20:49:48


CREATE TABLE QRTZ_JOB_DETAILS(
SCHED_NAME VARCHAR(120) NOT NULL,
JOB_NAME VARCHAR(200) NOT NULL,
JOB_GROUP VARCHAR(200) NOT NULL,
DESCRIPTION VARCHAR(250) NULL,
JOB_CLASS_NAME VARCHAR(250) NOT NULL,
IS_DURABLE BOOLEAN NOT NULL,
IS_NONCONCURRENT BOOLEAN NOT NULL,
IS_UPDATE_DATA BOOLEAN NOT NULL,
REQUESTS_RECOVERY BOOLEAN NOT NULL,
JOB_DATA BLOB NULL,
PRIMARY KEY (SCHED_NAME,JOB_NAME,JOB_GROUP))
ENGINE=InnoDB;

CREATE TABLE QRTZ_TRIGGERS (
SCHED_NAME VARCHAR(120) NOT NULL,
TRIGGER_NAME VARCHAR(200) NOT NULL,
TRIGGER_GROUP VARCHAR(200) NOT NULL,
JOB_NAME VARCHAR(200) NOT NULL,
JOB_GROUP VARCHAR(200) NOT NULL,
DESCRIPTION VARCHAR(250) NULL,
NEXT_FIRE_TIME BIGINT(19) NULL,
PREV_FIRE_TIME BIGINT(19) NULL,
PRIORITY INTEGER NULL,
TRIGGER_STATE VARCHAR(16) NOT NULL,
TRIGGER_TYPE VARCHAR(8) NOT NULL,
START_TIME BIGINT(19) NOT NULL,
END_TIME BIGINT(19) NULL,
CALENDAR_NAME VARCHAR(200) NULL,
MISFIRE_INSTR SMALLINT(2) NULL,
JOB_DATA BLOB NULL,
PRIMARY KEY (SCHED_NAME,TRIGGER_NAME,TRIGGER_GROUP),
FOREIGN KEY (SCHED_NAME,JOB_NAME,JOB_GROUP)
REFERENCES QRTZ_JOB_DETAILS(SCHED_NAME,JOB_NAME,JOB_GROUP))
ENGINE=InnoDB;

CREATE TABLE QRTZ_SIMPLE_TRIGGERS (
SCHED_NAME VARCHAR(120) NOT NULL,
TRIGGER_NAME VARCHAR(200) NOT NULL,
TRIGGER_GROUP VARCHAR(200) NOT NULL,
REPEAT_COUNT BIGINT(7) NOT NULL,
REPEAT_INTERVAL BIGINT(12) NOT NULL,
TIMES_TRIGGERED BIGINT(10) NOT NULL,
PRIMARY KEY (SCHED_NAME,TRIGGER_NAME,TRIGGER_GROUP),
FOREIGN KEY (SCHED_NAME,TRIGGER_NAME,TRIGGER_GROUP)
REFERENCES QRTZ_TRIGGERS(SCHED_NAME,TRIGGER_NAME,TRIGGER_GROUP))
ENGINE=InnoDB;

CREATE TABLE QRTZ_CRON_TRIGGERS (
SCHED_NAME VARCHAR(120) NOT NULL,
TRIGGER_NAME VARCHAR(200) NOT NULL,
TRIGGER_GROUP VARCHAR(200) NOT NULL,
CRON_EXPRESSION VARCHAR(120) NOT NULL,
TIME_ZONE_ID VARCHAR(80),
PRIMARY KEY (SCHED_NAME,TRIGGER_NAME,TRIGGER_GROUP),
FOREIGN KEY (SCHED_NAME,TRIGGER_NAME,TRIGGER_GROUP)
REFERENCES QRTZ_TRIGGERS(SCHED_NAME,TRIGGER_NAME,TRIGGER_GROUP))
ENGINE=InnoDB;

CREATE TABLE QRTZ_SIMPROP_TRIGGERS
  (
    SCHED_NAME VARCHAR(120) NOT NULL,
    TRIGGER_NAME VARCHAR(200) NOT NULL,
    TRIGGER_GROUP VARCHAR(200) NOT NULL,
    STR_PROP_1 VARCHAR(512) NULL,
    STR_PROP_2 VARCHAR(512) NULL,
    STR_PROP_3 VARCHAR(512) NULL,
    INT_PROP_1 INT NULL,
    INT_PROP_2 INT NULL,
    LONG_PROP_1 BIGINT NULL,
    LONG_PROP_2 BIGINT NULL,
    DEC_PROP_1 NUMERIC(13,4) NULL,
    DEC_PROP_2 NUMERIC(13,4) NULL,
    BOOL_PROP_1 BOOLEAN NULL,
    BOOL_PROP_2 BOOLEAN NULL,
    TIME_ZONE_ID VARCHAR(80) NULL,
    PRIMARY KEY (SCHED_NAME,TRIGGER_NAME,TRIGGER_GROUP),
    FOREIGN KEY (SCHED_NAME,TRIGGER_NAME,TRIGGER_GROUP)
    REFERENCES QRTZ_TRIGGERS(SCHED_NAME,TRIGGER_NAME,TRIGGER_GROUP))
ENGINE=InnoDB;

CREATE TABLE QRTZ_BLOB_TRIGGERS (
SCHED_NAME VARCHAR(120) NOT NULL,
TRIGGER_NAME VARCHAR(200) NOT NULL,
TRIGGER_GROUP VARCHAR(200) NOT NULL,
BLOB_DATA BLOB NULL,
PRIMARY KEY (SCHED_NAME,TRIGGER_NAME,TRIGGER_GROUP),
INDEX (SCHED_NAME,TRIGGER_NAME, TRIGGER_GROUP),
FOREIGN KEY (SCHED_NAME,TRIGGER_NAME,TRIGGER_GROUP)
REFERENCES QRTZ_TRIGGERS(SCHED_NAME,TRIGGER_NAME,TRIGGER_GROUP))
ENGINE=InnoDB;

CREATE TABLE QRTZ_CALENDARS (
SCHED_NAME VARCHAR(120) NOT NULL,
CALENDAR_NAME VARCHAR(200) NOT NULL,
CALENDAR BLOB NOT NULL,
PRIMARY KEY (SCHED_NAME,CALENDAR_NAME))
ENGINE=InnoDB;

CREATE TABLE QRTZ_PAUSED_TRIGGER_GRPS (
SCHED_NAME VARCHAR(120) NOT NULL,
TRIGGER_GROUP VARCHAR(200) NOT NULL,
PRIMARY KEY (SCHED_NAME,TRIGGER_GROUP))
ENGINE=InnoDB;

CREATE TABLE QRTZ_FIRED_TRIGGERS (
SCHED_NAME VARCHAR(120) NOT NULL,
ENTRY_ID VARCHAR(140) NOT NULL,
TRIGGER_NAME VARCHAR(200) NOT NULL,
TRIGGER_GROUP VARCHAR(200) NOT NULL,
INSTANCE_NAME VARCHAR(200) NOT NULL,
FIRED_TIME BIGINT(19) NOT NULL,
SCHED_TIME BIGINT(19) NOT NULL,
PRIORITY INTEGER NOT NULL,
STATE VARCHAR(16) NOT NULL,
JOB_NAME VARCHAR(200) NULL,
JOB_GROUP VARCHAR(200) NULL,
IS_NONCONCURRENT BOOLEAN NULL,
REQUESTS_RECOVERY BOOLEAN NULL,
PRIMARY KEY (SCHED_NAME,ENTRY_ID))
ENGINE=InnoDB;

CREATE TABLE QRTZ_SCHEDULER_STATE (
SCHED_NAME VARCHAR(120) NOT NULL,
INSTANCE_NAME VARCHAR(200) NOT NULL,
LAST_CHECKIN_TIME BIGINT(19) NOT NULL,
CHECKIN_INTERVAL BIGINT(19) NOT NULL,
PRIMARY KEY (SCHED_NAME,INSTANCE_NAME))
ENGINE=InnoDB;

CREATE TABLE QRTZ_LOCKS (
SCHED_NAME VARCHAR(120) NOT NULL,
LOCK_NAME VARCHAR(40) NOT NULL,
PRIMARY KEY (SCHED_NAME,LOCK_NAME))
ENGINE=InnoDB;

CREATE INDEX IDX_QRTZ_J_REQ_RECOVERY ON QRTZ_JOB_DETAILS(SCHED_NAME,REQUESTS_RECOVERY);
CREATE INDEX IDX_QRTZ_J_GRP ON QRTZ_JOB_DETAILS(SCHED_NAME,JOB_GROUP);

CREATE INDEX IDX_QRTZ_T_J ON QRTZ_TRIGGERS(SCHED_NAME,JOB_NAME,JOB_GROUP);
CREATE INDEX IDX_QRTZ_T_JG ON QRTZ_TRIGGERS(SCHED_NAME,JOB_GROUP);
CREATE INDEX IDX_QRTZ_T_C ON QRTZ_TRIGGERS(SCHED_NAME,CALENDAR_NAME);
CREATE INDEX IDX_QRTZ_T_G ON QRTZ_TRIGGERS(SCHED_NAME,TRIGGER_GROUP);
CREATE INDEX IDX_QRTZ_T_STATE ON QRTZ_TRIGGERS(SCHED_NAME,TRIGGER_STATE);
CREATE INDEX IDX_QRTZ_T_N_STATE ON QRTZ_TRIGGERS(SCHED_NAME,TRIGGER_NAME,TRIGGER_GROUP,TRIGGER_STATE);
CREATE INDEX IDX_QRTZ_T_N_G_STATE ON QRTZ_TRIGGERS(SCHED_NAME,TRIGGER_GROUP,TRIGGER_STATE);
CREATE INDEX IDX_QRTZ_T_NEXT_FIRE_TIME ON QRTZ_TRIGGERS(SCHED_NAME,NEXT_FIRE_TIME);
CREATE INDEX IDX_QRTZ_T_NFT_ST ON QRTZ_TRIGGERS(SCHED_NAME,TRIGGER_STATE,NEXT_FIRE_TIME);
CREATE INDEX IDX_QRTZ_T_NFT_MISFIRE ON QRTZ_TRIGGERS(SCHED_NAME,MISFIRE_INSTR,NEXT_FIRE_TIME);
CREATE INDEX IDX_QRTZ_T_NFT_ST_MISFIRE ON QRTZ_TRIGGERS(SCHED_NAME,MISFIRE_INSTR,NEXT_FIRE_TIME,TRIGGER_STATE);
CREATE INDEX IDX_QRTZ_T_NFT_ST_MISFIRE_GRP ON QRTZ_TRIGGERS(SCHED_NAME,MISFIRE_INSTR,NEXT_FIRE_TIME,TRIGGER_GROUP,TRIGGER_STATE);

CREATE INDEX IDX_QRTZ_FT_TRIG_INST_NAME ON QRTZ_FIRED_TRIGGERS(SCHED_NAME,INSTANCE_NAME);
CREATE INDEX IDX_QRTZ_FT_INST_JOB_REQ_RCVRY ON QRTZ_FIRED_TRIGGERS(SCHED_NAME,INSTANCE_NAME,REQUESTS_RECOVERY);
CREATE INDEX IDX_QRTZ_FT_J_G ON QRTZ_FIRED_TRIGGERS(SCHED_NAME,JOB_NAME,JOB_GROUP);
CREATE INDEX IDX_QRTZ_FT_JG ON QRTZ_FIRED_TRIGGERS(SCHED_NAME,JOB_GROUP);
CREATE INDEX IDX_QRTZ_FT_T_G ON QRTZ_FIRED_TRIGGERS(SCHED_NAME,TRIGGER_NAME,TRIGGER_GROUP);
CREATE INDEX IDX_QRTZ_FT_TG ON QRTZ_FIRED_TRIGGERS(SCHED_NAME,TRIGGER_GROUP);

    create table Attachment (
        id bigint not null auto_increment,
        accessType integer,
        attachedAt datetime,
        -- attachedAt datetime(6), to be used with mysql 5.6.4 that supports millis precision
        attachmentContentId bigint not null,
        contentType varchar(255),
        name varchar(255),
        attachment_size integer,
        attachedBy_id varchar(255),
        TaskData_Attachments_Id bigint,
        primary key (id)
    );

    create table AuditTaskImpl (
        id bigint not null auto_increment,
        activationTime datetime,
        -- activationTime datetime(6), to be used with mysql 5.6.4 that supports millis precision
        actualOwner varchar(255),
        createdBy varchar(255),
        createdOn datetime,
        -- createdOn datetime(6), to be used with mysql 5.6.4 that supports millis precision
        deploymentId varchar(255),
        description varchar(255),
        dueDate datetime,
        -- dueDate datetime(6), to be used with mysql 5.6.4 that supports millis precision
        name varchar(255),
        parentId bigint not null,
        priority integer not null,
        processId varchar(255),
        processInstanceId bigint not null,
        processSessionId bigint not null,
        status varchar(255),
        taskId bigint,
        workItemId bigint,
        lastModificationDate datetime,
        primary key (id)
    );

    create table BAMTaskSummary (
        pk bigint not null auto_increment,
        createdDate datetime,
        -- createdDate datetime(6), to be used with mysql 5.6.4 that supports millis precision
        duration bigint,
        endDate datetime,
        -- endDate datetime(6), to be used with mysql 5.6.4 that supports millis precision
        processInstanceId bigint not null,
        startDate datetime,
        -- startDate datetime(6), to be used with mysql 5.6.4 that supports millis precision
        status varchar(255),
        taskId bigint not null,
        taskName varchar(255),
        userId varchar(255),
        OPTLOCK integer,
        primary key (pk)
    );

    create table BooleanExpression (
        id bigint not null auto_increment,
        expression longtext,
        type varchar(255),
        Escalation_Constraints_Id bigint,
        primary key (id)
    );
    
    create table CaseIdInfo (
        id bigint not null auto_increment,
        caseIdPrefix varchar(255),
        currentValue bigint,
        primary key (id)
    );
    
    create table CaseFileDataLog (
        id bigint not null auto_increment,
        caseDefId varchar(255),
        caseId varchar(255),
        itemName varchar(255),
        itemType varchar(255),
        itemValue varchar(255),
        lastModified datetime,
        lastModifiedBy varchar(255),
        primary key (id)
    );

    create table CaseRoleAssignmentLog (
        id bigint not null auto_increment,
        caseId varchar(255),
        entityId varchar(255),
        processInstanceId bigint not null,
        roleName varchar(255),
        type integer not null,
        primary key (id)
    );

    create table Content (
        id bigint not null auto_increment,
        content longblob,
        primary key (id)
    );

    create table ContextMappingInfo (
        mappingId bigint not null auto_increment,
        CONTEXT_ID varchar(255) not null,
        KSESSION_ID bigint not null,
        OWNER_ID varchar(255),
        OPTLOCK integer,
        primary key (mappingId)
    );

    create table CorrelationKeyInfo (
        keyId bigint not null auto_increment,
        name varchar(255),
        processInstanceId bigint not null,
        OPTLOCK integer,
        primary key (keyId)
    );

    create table CorrelationPropertyInfo (
        propertyId bigint not null auto_increment,
        name varchar(255),
        value varchar(255),
        OPTLOCK integer,
        correlationKey_keyId bigint,
        primary key (propertyId)
    );

    create table Deadline (
        id bigint not null auto_increment,
        deadline_date datetime,
        -- deadline_date datetime(6), to be used with mysql 5.6.4 that supports millis precision
        escalated smallint,
        Deadlines_StartDeadLine_Id bigint,
        Deadlines_EndDeadLine_Id bigint,
        primary key (id)
    );

    create table Delegation_delegates (
        task_id bigint not null,
        entity_id varchar(255) not null
    );

    create table DeploymentStore (
        id bigint not null auto_increment,
        attributes varchar(255),
        DEPLOYMENT_ID varchar(255),
        deploymentUnit longtext,
        state integer,
        updateDate datetime,
        -- updateDate datetime(6), to be used with mysql 5.6.4 that supports millis precision
        primary key (id)
    );

    create table ErrorInfo (
        id bigint not null auto_increment,
        message varchar(255),
        stacktrace varchar(5000),
        timestamp datetime,
        -- timestamp datetime(6), to be used with mysql 5.6.4 that supports millis precision
        REQUEST_ID bigint not null,
        primary key (id)
    );

    create table Escalation (
        id bigint not null auto_increment,
        name varchar(255),
        Deadline_Escalation_Id bigint,
        primary key (id)
    );

    create table EventTypes (
        InstanceId bigint not null,
        element varchar(255)
    );
    
    create table ExecutionErrorInfo (
        id bigint not null auto_increment,
        ERROR_ACK smallint,
        ERROR_ACK_AT datetime,
        ERROR_ACK_BY varchar(255),
        ACTIVITY_ID bigint,
        ACTIVITY_NAME varchar(255),
        DEPLOYMENT_ID varchar(255),
        ERROR_INFO longtext,
        ERROR_DATE datetime,
        ERROR_ID varchar(255),
        ERROR_MSG varchar(255),
        INIT_ACTIVITY_ID bigint,
        JOB_ID bigint,
        PROCESS_ID varchar(255),
        PROCESS_INST_ID bigint,
        ERROR_TYPE varchar(255),
        primary key (id)
    );

    create table I18NText (
        id bigint not null auto_increment,
        language varchar(255),
        shortText varchar(255),
        text longtext,
        Task_Subjects_Id bigint,
        Task_Names_Id bigint,
        Task_Descriptions_Id bigint,
        Reassignment_Documentation_Id bigint,
        Notification_Subjects_Id bigint,
        Notification_Names_Id bigint,
        Notification_Documentation_Id bigint,
        Notification_Descriptions_Id bigint,
        Deadline_Documentation_Id bigint,
        primary key (id)
    );

    create table NodeInstanceLog (
        id bigint not null auto_increment,
        connection varchar(255),
        log_date datetime,
        -- log_date datetime(6), to be used with mysql 5.6.4 that supports millis precision
        externalId varchar(255),
        nodeId varchar(255),
        nodeInstanceId varchar(255),
        nodeName varchar(255),
        nodeType varchar(255),
        processId varchar(255),
        processInstanceId bigint not null,
        sla_due_date datetime,
        -- sla_due_date datetime(6), to be used with mysql 5.6.4 that supports millis precision
        slaCompliance integer,
        type integer not null,
        workItemId bigint,
        nodeContainerId varchar(255),
        referenceId bigint,
        primary key (id)
    );

    create table Notification (
        DTYPE varchar(31) not null,
        id bigint not null auto_increment,
        priority integer not null,
        Escalation_Notifications_Id bigint,
        primary key (id)
    );

    create table Notification_BAs (
        task_id bigint not null,
        entity_id varchar(255) not null
    );

    create table Notification_Recipients (
        task_id bigint not null,
        entity_id varchar(255) not null
    );

    create table Notification_email_header (
        Notification_id bigint not null,
        emailHeaders_id bigint not null,
        mapkey varchar(255) not null,
        primary key (Notification_id, mapkey)
    );

    create table OrganizationalEntity (
        DTYPE varchar(31) not null,
        id varchar(255) not null,
        primary key (id)
    );

    create table PeopleAssignments_BAs (
        task_id bigint not null,
        entity_id varchar(255) not null
    );

    create table PeopleAssignments_ExclOwners (
        task_id bigint not null,
        entity_id varchar(255) not null
    );

    create table PeopleAssignments_PotOwners (
        task_id bigint not null,
        entity_id varchar(255) not null
    );

    create table PeopleAssignments_Recipients (
        task_id bigint not null,
        entity_id varchar(255) not null
    );

    create table PeopleAssignments_Stakeholders (
        task_id bigint not null,
        entity_id varchar(255) not null
    );

    create table ProcessInstanceInfo (
        InstanceId bigint not null auto_increment,
        lastModificationDate datetime,
        -- lastModificationDate datetime(6), to be used with mysql 5.6.4 that supports millis precision
        lastReadDate datetime,
        -- lastReadDate datetime(6), to be used with mysql 5.6.4 that supports millis precision
        processId varchar(255),
        processInstanceByteArray longblob,
        startDate datetime,
        -- startDate datetime(6), to be used with mysql 5.6.4 that supports millis precision
        state integer not null,
        OPTLOCK integer,
        primary key (InstanceId)
    );

    create table ProcessInstanceLog (
        id bigint not null auto_increment,
        correlationKey varchar(255),
        duration bigint,
        end_date datetime,
        -- end_date datetime(6), to be used with mysql 5.6.4 that supports millis precision
        externalId varchar(255),
        user_identity varchar(255),
        outcome varchar(255),
        parentProcessInstanceId bigint,
        processId varchar(255),
        processInstanceDescription varchar(255),
        processInstanceId bigint not null,
        processName varchar(255),
        processType integer,
        processVersion varchar(255),
        sla_due_date datetime,
        -- sla_due_date datetime(6), to be used with mysql 5.6.4 that supports millis precision
        slaCompliance integer,
        start_date datetime,
        -- start_date datetime(6), to be used with mysql 5.6.4 that supports millis precision
        status integer,
        primary key (id)
    );

    create table QueryDefinitionStore (
        id bigint not null auto_increment,
        qExpression longtext,
        qName varchar(255),
        qSource varchar(255),
        qTarget varchar(255),
        primary key (id)
    );

    create table Reassignment (
        id bigint not null auto_increment,
        Escalation_Reassignments_Id bigint,
        primary key (id)
    );

    create table Reassignment_potentialOwners (
        task_id bigint not null,
        entity_id varchar(255) not null
    );

    create table RequestInfo (
        id bigint not null auto_increment,
        commandName varchar(255),
        deploymentId varchar(255),
        executions integer not null,
        businessKey varchar(255),
        message varchar(255),
        owner varchar(255),
        priority integer not null,
        processInstanceId bigint,
        requestData longblob,
        responseData longblob,
        retries integer not null,
        status varchar(255),
        timestamp datetime,
        -- timestamp datetime(6), to be used with mysql 5.6.4 that supports millis precision
        primary key (id)
    );

    create table SessionInfo (
        id bigint not null auto_increment,
        lastModificationDate datetime,
        -- lastModificationDate datetime(6), to be used with mysql 5.6.4 that supports millis precision
        rulesByteArray longblob,
        startDate datetime,
        -- startDate datetime(6), to be used with mysql 5.6.4 that supports millis precision
        OPTLOCK integer,
        primary key (id)
    );

    create table Task (
        id bigint not null auto_increment,
        archived smallint,
        allowedToDelegate varchar(255),
        description varchar(255),
        formName varchar(255),
        name varchar(255),
        priority integer not null,
        subTaskStrategy varchar(255),
        subject varchar(255),
        activationTime datetime,
        -- activationTime datetime(6), to be used with mysql 5.6.4 that supports millis precision
        createdOn datetime,
        -- createdOn datetime(6), to be used with mysql 5.6.4 that supports millis precision
        deploymentId varchar(255),
        documentAccessType integer,
        documentContentId bigint not null,
        documentType varchar(255),
        expirationTime datetime,
        -- expirationTime datetime(6), to be used with mysql 5.6.4 that supports millis precision
        faultAccessType integer,
        faultContentId bigint not null,
        faultName varchar(255),
        faultType varchar(255),
        outputAccessType integer,
        outputContentId bigint not null,
        outputType varchar(255),
        parentId bigint not null,
        previousStatus integer,
        processId varchar(255),
        processInstanceId bigint not null,
        processSessionId bigint not null,
        skipable boolean not null,
        status varchar(255),
        workItemId bigint not null,
        taskType varchar(255),
        OPTLOCK integer,
        taskInitiator_id varchar(255),
        actualOwner_id varchar(255),
        createdBy_id varchar(255),
        primary key (id)
    );

    create table TaskDef (
        id bigint not null auto_increment,
        name varchar(255),
        priority integer not null,
        primary key (id)
    );

    create table TaskEvent (
        id bigint not null auto_increment,
        logTime datetime,
        -- logTime datetime(6), to be used with mysql 5.6.4 that supports millis precision
        message varchar(255),
        processInstanceId bigint,
        taskId bigint,
        type varchar(255),
        userId varchar(255),
        OPTLOCK integer,
        workItemId bigint,
        primary key (id)
    );

    create table TaskVariableImpl (
        id bigint not null auto_increment,
        modificationDate datetime,
        -- modificationDate datetime(6), to be used with mysql 5.6.4 that supports millis precision
        name varchar(255),
        processId varchar(255),
        processInstanceId bigint,
        taskId bigint,
        type integer,
        value varchar(4000),
        primary key (id)
    );

    create table VariableInstanceLog (
        id bigint not null auto_increment,
        log_date datetime,
        -- log_date datetime(6), to be used with mysql 5.6.4 that supports millis precision
        externalId varchar(255),
        oldValue varchar(255),
        processId varchar(255),
        processInstanceId bigint not null,
        value varchar(255),
        variableId varchar(255),
        variableInstanceId varchar(255),
        primary key (id)
    );

    create table WorkItemInfo (
        workItemId bigint not null auto_increment,
        creationDate datetime,
        -- creationDate datetime(6), to be used with mysql 5.6.4 that supports millis precision
        name varchar(255),
        processInstanceId bigint not null,
        state bigint not null,
        OPTLOCK integer,
        workItemByteArray longblob,
        primary key (workItemId)
    );

    create table email_header (
        id bigint not null auto_increment,
        body longtext,
        fromAddress varchar(255),
        language varchar(255),
        replyToAddress varchar(255),
        subject varchar(255),
        primary key (id)
    );

    create table task_comment (
        id bigint not null auto_increment,
        addedAt datetime,
        -- addedAt datetime(6), to be used with mysql 5.6.4 that supports millis precision
        text longtext,
        addedBy_id varchar(255),
        TaskData_Comments_Id bigint,
        primary key (id)
    );

    alter table DeploymentStore 
        add constraint UK_85rgskt09thd8mkkfl3tb0y81 unique (DEPLOYMENT_ID);

    alter table QueryDefinitionStore 
        add constraint UK_4ry5gt77jvq0orfttsoghta2j unique (qName);

    alter table Attachment 
        add index IDX_Attachment_Id (attachedBy_id), 
        add constraint FK_7ndpfa311i50bq7hy18q05va3 
        foreign key (attachedBy_id) 
        references OrganizationalEntity (id);

    alter table Attachment 
        add index IDX_Attachment_DataId (TaskData_Attachments_Id), 
        add constraint FK_hqupx569krp0f0sgu9kh87513 
        foreign key (TaskData_Attachments_Id) 
        references Task (id);

    alter table BooleanExpression 
        add index IDX_BoolExpr_Id (Escalation_Constraints_Id), 
        add constraint FK_394nf2qoc0k9ok6omgd6jtpso 
        foreign key (Escalation_Constraints_Id) 
        references Escalation (id);
        
    alter table CaseIdInfo 
        add constraint UK_CaseIdInfo_1 unique (caseIdPrefix);        

    alter table CorrelationPropertyInfo 
        add index IDX_CorrPropInfo_Id (correlationKey_keyId), 
        add constraint FK_hrmx1m882cejwj9c04ixh50i4 
        foreign key (correlationKey_keyId) 
        references CorrelationKeyInfo (keyId);

    alter table Deadline 
        add index IDX_Deadline_StartId (Deadlines_StartDeadLine_Id), 
        add constraint FK_68w742sge00vco2cq3jhbvmgx 
        foreign key (Deadlines_StartDeadLine_Id) 
        references Task (id);

    alter table Deadline 
        add index IDX_Deadline_EndId (Deadlines_EndDeadLine_Id), 
        add constraint FK_euoohoelbqvv94d8a8rcg8s5n 
        foreign key (Deadlines_EndDeadLine_Id) 
        references Task (id);

    alter table Delegation_delegates 
        add index IDX_Delegation_EntityId (entity_id), 
        add constraint FK_gn7ula51sk55wj1o1m57guqxb 
        foreign key (entity_id) 
        references OrganizationalEntity (id);

    alter table Delegation_delegates 
        add index IDX_Delegation_TaskId (task_id), 
        add constraint FK_fajq6kossbsqwr3opkrctxei3 
        foreign key (task_id) 
        references Task (id);

    alter table ErrorInfo 
        add index IDX_ErrorInfo_Id (REQUEST_ID), 
        add constraint FK_cms0met37ggfw5p5gci3otaq0 
        foreign key (REQUEST_ID) 
        references RequestInfo (id);

    alter table Escalation 
        add index IDX_Escalation_Id (Deadline_Escalation_Id), 
        add constraint FK_ay2gd4fvl9yaapviyxudwuvfg 
        foreign key (Deadline_Escalation_Id) 
        references Deadline (id);

    alter table I18NText 
        add index IDX_I18NText_SubjId (Task_Subjects_Id), 
        add constraint FK_k16jpgrh67ti9uedf6konsu1p 
        foreign key (Task_Subjects_Id) 
        references Task (id);

    alter table I18NText 
        add index IDX_I18NText_NameId (Task_Names_Id), 
        add constraint FK_fd9uk6hemv2dx1ojovo7ms3vp 
        foreign key (Task_Names_Id) 
        references Task (id);

    alter table I18NText 
        add index Task_Descriptions_Id (Task_Descriptions_Id), 
        add constraint FK_4eyfp69ucrron2hr7qx4np2fp 
        foreign key (Task_Descriptions_Id) 
        references Task (id);

    alter table I18NText 
        add index IDX_I18NText_ReassignId (Reassignment_Documentation_Id), 
        add constraint FK_pqarjvvnwfjpeyb87yd7m0bfi 
        foreign key (Reassignment_Documentation_Id) 
        references Reassignment (id);

    alter table I18NText 
        add index IDX_I18NText_NotSubjId (Notification_Subjects_Id), 
        add constraint FK_o84rkh69r47ti8uv4eyj7bmo2 
        foreign key (Notification_Subjects_Id) 
        references Notification (id);

    alter table I18NText 
        add index IDX_I18NText_NotNamId (Notification_Names_Id), 
        add constraint FK_g1trxri8w64enudw2t1qahhk5 
        foreign key (Notification_Names_Id) 
        references Notification (id);

    alter table I18NText 
        add index IDX_I18NText_NotDocId (Notification_Documentation_Id), 
        add constraint FK_qoce92c70adem3ccb3i7lec8x 
        foreign key (Notification_Documentation_Id) 
        references Notification (id);

    alter table I18NText 
        add index IDX_I18NText_NotDescrId (Notification_Descriptions_Id), 
        add constraint FK_bw8vmpekejxt1ei2ge26gdsry 
        foreign key (Notification_Descriptions_Id) 
        references Notification (id);

    alter table I18NText 
        add index IDX_I18NText_DeadDocId (Deadline_Documentation_Id), 
        add constraint FK_21qvifarxsvuxeaw5sxwh473w 
        foreign key (Deadline_Documentation_Id) 
        references Deadline (id);

    alter table Notification 
        add index IDX_Not_EscId (Escalation_Notifications_Id), 
        add constraint FK_bdbeml3768go5im41cgfpyso9 
        foreign key (Escalation_Notifications_Id) 
        references Escalation (id);

    alter table Notification_BAs 
        add index IDX_NotBAs_Entity (entity_id), 
        add constraint FK_mfbsnbrhth4rjhqc2ud338s4i 
        foreign key (entity_id) 
        references OrganizationalEntity (id);

    alter table Notification_BAs 
        add index IDX_NotBAs_Task (task_id), 
        add constraint FK_fc0uuy76t2bvxaxqysoo8xts7 
        foreign key (task_id) 
        references Notification (id);

    alter table Notification_Recipients 
        add index IDX_NotRec_Entity (entity_id), 
        add constraint FK_blf9jsrumtrthdaqnpwxt25eu 
        foreign key (entity_id) 
        references OrganizationalEntity (id);

    alter table Notification_Recipients 
        add index IDX_NotRec_Task (task_id), 
        add constraint FK_3l244pj8sh78vtn9imaymrg47 
        foreign key (task_id) 
        references Notification (id);

    alter table Notification_email_header 
        add constraint IDX_NotEmail_Header unique  (emailHeaders_id), 
        add constraint FK_ptaka5kost68h7l3wflv7w6y8 
        foreign key (emailHeaders_id) 
        references email_header (id);

    alter table Notification_email_header 
        add index IDX_NotEmail_Not (Notification_id), 
        add constraint FK_eth4nvxn21fk1vnju85vkjrai 
        foreign key (Notification_id) 
        references Notification (id);

    alter table PeopleAssignments_BAs 
        add index IDX_PAsBAs_Entity (entity_id), 
        add constraint FK_t38xbkrq6cppifnxequhvjsl2 
        foreign key (entity_id) 
        references OrganizationalEntity (id);

    alter table PeopleAssignments_BAs 
        add index IDX_PAsBAs_Task (task_id), 
        add constraint FK_omjg5qh7uv8e9bolbaq7hv6oh 
        foreign key (task_id) 
        references Task (id);

    alter table PeopleAssignments_ExclOwners 
        add index IDX_PAsExcl_Entity (entity_id), 
        add constraint FK_pth28a73rj6bxtlfc69kmqo0a 
        foreign key (entity_id) 
        references OrganizationalEntity (id);

    alter table PeopleAssignments_ExclOwners 
        add index IDX_PAsExcl_Task (task_id), 
        add constraint FK_b8owuxfrdng050ugpk0pdowa7 
        foreign key (task_id) 
        references Task (id);

    alter table PeopleAssignments_PotOwners 
        add index IDX_PAsPot_Entity (entity_id), 
        add constraint FK_tee3ftir7xs6eo3fdvi3xw026 
        foreign key (entity_id) 
        references OrganizationalEntity (id);

    alter table PeopleAssignments_PotOwners 
        add index IDX_PAsPot_Task (task_id), 
        add constraint FK_4dv2oji7pr35ru0w45trix02x 
        foreign key (task_id) 
        references Task (id);

    alter table PeopleAssignments_Recipients 
        add index IDX_PAsRecip_Entity (entity_id), 
        add constraint FK_4g7y3wx6gnokf6vycgpxs83d6 
        foreign key (entity_id) 
        references OrganizationalEntity (id);

    alter table PeopleAssignments_Recipients 
        add index IDX_PAsRecip_Task (task_id), 
        add constraint FK_enhk831fghf6akjilfn58okl4 
        foreign key (task_id) 
        references Task (id);

    alter table PeopleAssignments_Stakeholders 
        add index IDX_PAsStake_Entity (entity_id), 
        add constraint FK_met63inaep6cq4ofb3nnxi4tm 
        foreign key (entity_id) 
        references OrganizationalEntity (id);

    alter table PeopleAssignments_Stakeholders 
        add index IDX_PAsStake_Task (task_id), 
        add constraint FK_4bh3ay74x6ql9usunubttfdf1 
        foreign key (task_id) 
        references Task (id);

    alter table Reassignment 
        add index IDX_Reassign_Esc (Escalation_Reassignments_Id), 
        add constraint FK_pnpeue9hs6kx2ep0sp16b6kfd 
        foreign key (Escalation_Reassignments_Id) 
        references Escalation (id);

    alter table Reassignment_potentialOwners 
        add index IDX_ReassignPO_Entity (entity_id), 
        add constraint FK_8frl6la7tgparlnukhp8xmody 
        foreign key (entity_id) 
        references OrganizationalEntity (id);

    alter table Reassignment_potentialOwners 
        add index IDX_ReassignPO_Task (task_id), 
        add constraint FK_qbega5ncu6b9yigwlw55aeijn 
        foreign key (task_id) 
        references Reassignment (id);

    alter table Task 
        add index IDX_Task_Initiator (taskInitiator_id), 
        add constraint FK_dpk0f9ucm14c78bsxthh7h8yh 
        foreign key (taskInitiator_id) 
        references OrganizationalEntity (id);

    alter table Task 
        add index IDX_Task_ActualOwner (actualOwner_id), 
        add constraint FK_nh9nnt47f3l61qjlyedqt05rf 
        foreign key (actualOwner_id) 
        references OrganizationalEntity (id);

    alter table Task 
        add index IDX_Task_CreatedBy (createdBy_id), 
        add constraint FK_k02og0u71obf1uxgcdjx9rcjc 
        foreign key (createdBy_id) 
        references OrganizationalEntity (id);

    alter table task_comment 
        add index IDX_TaskComments_CreatedBy (addedBy_id), 
        add constraint FK_aax378yjnsmw9kb9vsu994jjv 
        foreign key (addedBy_id) 
        references OrganizationalEntity (id);

    alter table task_comment 
        add index IDX_TaskComments_Id (TaskData_Comments_Id), 
        add constraint FK_1ws9jdmhtey6mxu7jb0r0ufvs 
        foreign key (TaskData_Comments_Id) 
        references Task (id);

    create index IDX_Task_processInstanceId on Task(processInstanceId);
    create index IDX_Task_processId on Task(processId);
    create index IDX_Task_status on Task(status);
    create index IDX_Task_archived on Task(archived);
    create index IDX_Task_workItemId on Task(workItemId);
    
    create index IDX_EventTypes_element ON EventTypes(element);
    create index IDX_EventTypes_compound ON EventTypes(InstanceId, element);

    create index IDX_CMI_Context ON ContextMappingInfo(CONTEXT_ID);    
    create index IDX_CMI_KSession ON ContextMappingInfo(KSESSION_ID);    
    create index IDX_CMI_Owner ON ContextMappingInfo(OWNER_ID);
    
    create index IDX_RequestInfo_timestamp ON RequestInfo(timestamp);
    create index IDX_RequestInfo_owner ON RequestInfo(owner);
    
    create index IDX_BAMTaskSumm_createdDate on BAMTaskSummary(createdDate);
    create index IDX_BAMTaskSumm_duration on BAMTaskSummary(duration);
    create index IDX_BAMTaskSumm_endDate on BAMTaskSummary(endDate);
    create index IDX_BAMTaskSumm_pInstId on BAMTaskSummary(processInstanceId);
    create index IDX_BAMTaskSumm_startDate on BAMTaskSummary(startDate);
    create index IDX_BAMTaskSumm_status on BAMTaskSummary(status);
    create index IDX_BAMTaskSumm_taskId on BAMTaskSummary(taskId);
    create index IDX_BAMTaskSumm_taskName on BAMTaskSummary(taskName);
    create index IDX_BAMTaskSumm_userId on BAMTaskSummary(userId);
    
    create index IDX_PInstLog_duration on ProcessInstanceLog(duration);
    create index IDX_PInstLog_end_date on ProcessInstanceLog(end_date);
    create index IDX_PInstLog_extId on ProcessInstanceLog(externalId);
    create index IDX_PInstLog_user_identity on ProcessInstanceLog(user_identity);
    create index IDX_PInstLog_outcome on ProcessInstanceLog(outcome);
    create index IDX_PInstLog_parentPInstId on ProcessInstanceLog(parentProcessInstanceId);
    create index IDX_PInstLog_pId on ProcessInstanceLog(processId);
    create index IDX_PInstLog_pInsteDescr on ProcessInstanceLog(processInstanceDescription);
    create index IDX_PInstLog_pInstId on ProcessInstanceLog(processInstanceId);
    create index IDX_PInstLog_pName on ProcessInstanceLog(processName);
    create index IDX_PInstLog_pVersion on ProcessInstanceLog(processVersion);
    create index IDX_PInstLog_start_date on ProcessInstanceLog(start_date);
    create index IDX_PInstLog_status on ProcessInstanceLog(status);
    create index IDX_PInstLog_correlation on ProcessInstanceLog(correlationKey);

    create index IDX_VInstLog_pInstId on VariableInstanceLog(processInstanceId);
    create index IDX_VInstLog_varId on VariableInstanceLog(variableId);
    create index IDX_VInstLog_pId on VariableInstanceLog(processId);

    create index IDX_NInstLog_pInstId on NodeInstanceLog(processInstanceId);
    create index IDX_NInstLog_nodeType on NodeInstanceLog(nodeType);
    create index IDX_NInstLog_pId on NodeInstanceLog(processId);

    create index IDX_ErrorInfo_pInstId on ExecutionErrorInfo(PROCESS_INST_ID);
    create index IDX_ErrorInfo_errorAck on ExecutionErrorInfo(ERROR_ACK);

    create index IDX_AuditTaskImpl_taskId on AuditTaskImpl(taskId);
    create index IDX_AuditTaskImpl_pInstId on AuditTaskImpl(processInstanceId);
    create index IDX_AuditTaskImpl_workItemId on AuditTaskImpl(workItemId);
    create index IDX_AuditTaskImpl_name on AuditTaskImpl(name);
    create index IDX_AuditTaskImpl_processId on AuditTaskImpl(processId);
    create index IDX_AuditTaskImpl_status on AuditTaskImpl(status);

    create index IDX_TaskVariableImpl_taskId on TaskVariableImpl(taskId);
    create index IDX_TaskVariableImpl_pInstId on TaskVariableImpl(processInstanceId);
    create index IDX_TaskVariableImpl_processId on TaskVariableImpl(processId);

commit;
