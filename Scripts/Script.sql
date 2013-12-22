--
-- Table structure for table `Strategies`
--

DROP TABLE IF EXISTS `Strategies`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `Strategies` (
  `name` char(30) NOT NULL,
  PRIMARY KEY (`name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

DROP TABLE IF EXISTS `Orders`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `Orders` (
  `id` int(10) unsigned NOT NULL AUTO_INCREMENT,
  `orderid` int(10) unsigned DEFAULT NULL,
  `strategyname` char(30) NOT NULL,
  `period` char(15) NOT NULL,
  `laststate` char(15) NOT NULL,
  `profit` float DEFAULT NULL,
  `start` datetime NOT NULL,
  `close` datetime DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `Orders_Strategies_FK` (`strategyname`),
  CONSTRAINT `Orders_Strategies_FK` FOREIGN KEY (`strategyname`) REFERENCES `Strategies` (`name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;