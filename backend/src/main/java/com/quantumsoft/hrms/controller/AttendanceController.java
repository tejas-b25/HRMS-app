package com.quantumsoft.hrms.controller;

import com.quantumsoft.hrms.dto.ClockInInput;
import com.quantumsoft.hrms.dto.ReportInput;
import com.quantumsoft.hrms.entity.Attendance;
import com.quantumsoft.hrms.servicei.AttendanceServiceI;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@CrossOrigin("*")
@RestController
@RequestMapping(value = "/api/attendance")
public class AttendanceController {

    @Autowired
    private AttendanceServiceI attendanceService;

    @PreAuthorize("hasRole('EMPLOYEE') or hasRole('MANAGER') or hasRole('FINANCE')")
    @PostMapping(value = "/clockIn")
    public ResponseEntity<String> clockIn(@RequestBody ClockInInput clockInInput){
        return new ResponseEntity<String>(attendanceService.clockIn(clockInInput), HttpStatus.CREATED);
    }

    @PreAuthorize("hasRole('EMPLOYEE') or hasRole('MANAGER') or hasRole('FINANCE')")
    @PatchMapping(value = "/clockOut")
    public ResponseEntity<String> clockOut(){
        String message = attendanceService.clockOut();
        return new ResponseEntity<String>(message, HttpStatus.CREATED);
    }
    @PreAuthorize("hasRole('HR')")
    @PostMapping(value = "/pdf", consumes = "application/json", produces="application/pdf")
    public ResponseEntity<Resource> generatePdfReport(@RequestBody ReportInput reportInput) throws FileNotFoundException {
        Resource resource = attendanceService.generatePdfReport(reportInput);
        return new ResponseEntity<Resource>(resource, HttpStatus.OK);
    }

    @PreAuthorize("hasRole('HR')")
    @PostMapping(value = "/csv", consumes = "application/json", produces="text/csv")
    public ResponseEntity<Resource> generateCsvReport(@RequestBody ReportInput reportInput) throws IOException {
        Resource resource = attendanceService.generateCsvReport(reportInput);
        return new ResponseEntity<Resource>(resource, HttpStatus.OK);
    }

    @PreAuthorize("hasRole('HR')")
    @GetMapping(value = "/monthlyStatus")
    public ResponseEntity<List<Attendance>> fetchMonthlyAttendanceStatus(@RequestParam LocalDate fromDate, @RequestParam LocalDate toDate, @RequestParam UUID empId){
        List<Attendance> records = attendanceService.fetchMonthlyAttendanceStatus(fromDate, toDate, empId);
        return new ResponseEntity<List<Attendance>>(records, HttpStatus.OK);
    }
}
