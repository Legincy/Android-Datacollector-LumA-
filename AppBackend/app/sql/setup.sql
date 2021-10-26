-- Host: 127.0.0.1
-- Erstellungszeit: 26. Okt 2021 um 14:09
-- Server-Version: 10.5.11-MariaDB
-- PHP-Version: 7.4.1

SET SQL_MODE = "NO_AUTO_VALUE_ON_ZERO";
SET AUTOCOMMIT = 0;
START TRANSACTION;
SET time_zone = "+00:00";


/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!40101 SET NAMES utf8mb4 */;

--
-- Datenbank: `sensor`
--

DROP DATABASE IF EXISTS `sensor`;
CREATE DATABASE `sensor`;

CREATE USER IF NOT EXISTS 'app'@'%' IDENTIFIED BY PASSWORD '*C411277460BA1800D9A66DE1B61826B09D6DE4FD';
GRANT ALL PRIVILEGES ON `sensor`.* TO `app`@`%` IDENTIFIED BY PASSWORD '*C411277460BA1800D9A66DE1B61826B09D6DE4FD' WITH GRANT OPTION;

-- --------------------------------------------------------

--
-- Tabellenstruktur für Tabelle `accelerometer`
--

CREATE TABLE `accelerometer` (
  `id` int(11) NOT NULL,
  `timestamp` timestamp NOT NULL DEFAULT current_timestamp(),
  `x` float NOT NULL,
  `y` float NOT NULL,
  `z` float NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- --------------------------------------------------------

--
-- Tabellenstruktur für Tabelle `gyroscope`
--

CREATE TABLE `gyroscope` (
  `id` int(11) NOT NULL,
  `timestamp` timestamp NOT NULL DEFAULT current_timestamp(),
  `x` float NOT NULL,
  `y` float NOT NULL,
  `z` float NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- --------------------------------------------------------

--
-- Tabellenstruktur für Tabelle `light`
--

CREATE TABLE `light` (
  `id` int(11) NOT NULL,
  `timestamp` timestamp NOT NULL DEFAULT current_timestamp(),
  `value` float NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- --------------------------------------------------------

--
-- Tabellenstruktur für Tabelle `proximity`
--

CREATE TABLE `proximity` (
  `id` int(11) NOT NULL,
  `timestamp` timestamp NOT NULL DEFAULT current_timestamp(),
  `x` float NOT NULL,
  `y` float NOT NULL,
  `z` float NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

--
-- Indizes der exportierten Tabellen
--

--
-- Indizes für die Tabelle `accelerometer`
--
ALTER TABLE `accelerometer`
  ADD PRIMARY KEY (`id`);

--
-- Indizes für die Tabelle `gyroscope`
--
ALTER TABLE `gyroscope`
  ADD PRIMARY KEY (`id`);

--
-- Indizes für die Tabelle `light`
--
ALTER TABLE `light`
  ADD PRIMARY KEY (`id`);

--
-- Indizes für die Tabelle `proximity`
--
ALTER TABLE `proximity`
  ADD PRIMARY KEY (`id`);

--
-- AUTO_INCREMENT für exportierte Tabellen
--

--
-- AUTO_INCREMENT für Tabelle `accelerometer`
--
ALTER TABLE `accelerometer`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=2;

--
-- AUTO_INCREMENT für Tabelle `gyroscope`
--
ALTER TABLE `gyroscope`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=2;

--
-- AUTO_INCREMENT für Tabelle `light`
--
ALTER TABLE `light`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=4;

--
-- AUTO_INCREMENT für Tabelle `proximity`
--
ALTER TABLE `proximity`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=2;
COMMIT;

/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
