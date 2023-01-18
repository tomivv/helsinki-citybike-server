package com.tomivv.server.controller;

import java.io.Reader;
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
@RequestMapping("/api/v1/station")
public class StationController {

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

			int batchSize = 1000; // number of rows per batch
			int i = 0;
			String sqlDeparture = "INSERT INTO \"Departure_station\" (id, name, address, city, operator, capacity, x, y) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
			String sqlReturn = "INSERT INTO \"Return_station\" (id, name, address, city, operator, capacity, x, y) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
			while (i < records.size()) {
				int toIndex = i + batchSize;
				if (toIndex > records.size()) {
					toIndex = records.size();
				}
				List<CSVRecord> batch = records.subList(i, toIndex);
				jdbcTemplate.batchUpdate(sqlDeparture, new BatchPreparedStatementSetter() {
					@Override
					public void setValues(PreparedStatement ps, int j) throws SQLException {
						CSVRecord record = batch.get(j);

						ps.setInt(1, Integer.parseInt(record.get(1)));
						ps.setString(2, record.get(2));
						ps.setString(3, record.get(5));
						ps.setString(4, record.get(7));
						ps.setString(5, record.get(9));
						ps.setString(6, record.get(10));
						ps.setString(7, record.get(11));
						ps.setString(8, record.get(12));
					}
					@Override
					public int getBatchSize() {
						return batch.size();
					}
				});
				jdbcTemplate.batchUpdate(sqlReturn, new BatchPreparedStatementSetter() {
					@Override
					public void setValues(PreparedStatement ps, int j) throws SQLException {
						CSVRecord record = batch.get(j);

						ps.setInt(1, Integer.parseInt(record.get(1)));
						ps.setString(2, record.get(2));
						ps.setString(3, record.get(5));
						ps.setString(4, record.get(7));
						ps.setString(5, record.get(9));
						ps.setString(6, record.get(10));
						ps.setString(7, record.get(11));
						ps.setString(8, record.get(12));
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