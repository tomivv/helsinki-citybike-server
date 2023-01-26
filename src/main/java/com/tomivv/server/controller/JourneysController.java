package com.tomivv.server.controller;

import java.io.Reader;
import java.sql.Timestamp;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;

@RestController
@RequestMapping("/api/v1/journey")
public class JourneysController {

    @Autowired
	private JdbcTemplate jdbcTemplate;

	@CrossOrigin(origins = "*")
	@PostMapping("/bulk")
	public ResponseEntity<String> uploadCSV(@RequestBody MultipartFile file) {
		if (file.isEmpty()) {
			return new ResponseEntity<>("Please select csv file", HttpStatus.OK);
		}

		try {
			// read the CSV file
			Reader reader = new BufferedReader(new InputStreamReader(file.getInputStream()));
			CSVParser csvParser = new CSVParser(reader, CSVFormat.DEFAULT.builder().setHeader().setSkipHeaderRecord(true).build());
			List<CSVRecord> records = csvParser.getRecords();

			int batchSize = 1000;
			int i = 0;
			String sql = "INSERT INTO \"Journeys\" (departure, return, departure_station_id, return_station_id, distance, duration) " + 
                "SELECT ?, ?, ?, ?, ?, ? " + 
                "WHERE EXISTS (SELECT 1 FROM \"Departure_station\" WHERE id = ?) AND EXISTS (SELECT 1 FROM \"Return_station\" WHERE id = ?)";
			while (i < records.size()) {
				int toIndex = i + batchSize;
				if (toIndex > records.size()) {
					toIndex = records.size();
				}

                List<CSVRecord> batch = records.subList(i, toIndex);
				// remove journeys that dont have distance or duration
                batch.removeIf(r -> r.get(6) == "" || r.get(7) == "");
                // remove journeys that lasted under 10s or covered distance less than 10m and invalid timestamps
                batch.removeIf(r -> Double.parseDouble(r.get(6)) <= 10 || Double.parseDouble(r.get(7)) <= 10 || r.get(0).length() < 19 || r.get(1).length() < 19);

				jdbcTemplate.batchUpdate(sql, new BatchPreparedStatementSetter() {
					@Override
					public void setValues(PreparedStatement ps, int j) throws SQLException {
						CSVRecord record = batch.get(j);
						ps.setTimestamp(1, Timestamp.valueOf(record.get(0).replaceFirst("T", " ")));
						ps.setTimestamp(2, Timestamp.valueOf(record.get(1).replaceFirst("T", " ")));
						ps.setInt(3, Integer.parseInt(record.get(2)));
						ps.setInt(4, Integer.parseInt(record.get(4)));
						ps.setInt(5, (int)Double.parseDouble(record.get(6)));
						ps.setInt(6, (int)Double.parseDouble(record.get(7)));
                        ps.setInt(7, Integer.parseInt(record.get(2)));
						ps.setInt(8, Integer.parseInt(record.get(4)));
					}
					@Override
					public int getBatchSize() {
						return batch.size();
					}
				});
                i = toIndex;
            }
			csvParser.close();
			reader.close();
		} catch (IOException e) {
			e.printStackTrace();
			return new ResponseEntity<>("Failed to store data", HttpStatus.INTERNAL_SERVER_ERROR);
		}

		return new ResponseEntity<>("Data inserted to database successfully", HttpStatus.OK);
    }
}
