package com.zerotrust.iot.controller;

import com.zerotrust.iot.dto.dataset.DatasetBenchmarkResult;
import com.zerotrust.iot.dto.dataset.DatasetPresetDto;
import com.zerotrust.iot.service.DatasetIngestService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/dataset")
@Tag(name = "Kaggle Dataset Benchmark & Replay Studio", description = "Endpoints for replaying real-world Kaggle IoT datasets and computing confusion matrix defense metrics")
public class DatasetController {

    private final DatasetIngestService datasetIngestService;

    public DatasetController(DatasetIngestService datasetIngestService) {
        this.datasetIngestService = datasetIngestService;
    }

    @GetMapping("/presets")
    @Operation(summary = "List pre-bundled Kaggle IoT datasets (IoT-23 Malware, CIC-IoT-2023 DDoS)")
    public ResponseEntity<List<DatasetPresetDto>> getAvailablePresets() {
        return ResponseEntity.ok(datasetIngestService.getAvailablePresets());
    }

    @PostMapping("/replay-preset/{presetId}")
    @Operation(summary = "Replay a built-in Kaggle dataset preset and evaluate Zero Trust defense performance")
    public ResponseEntity<DatasetBenchmarkResult> replayPreset(
            @PathVariable String presetId,
            @RequestParam(required = false) UUID targetDeviceId
    ) {
        return ResponseEntity.ok(datasetIngestService.replayPreset(presetId, targetDeviceId));
    }

    @PostMapping(value = "/upload-replay", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Upload custom Kaggle CSV dataset and evaluate Zero Trust defense metrics")
    public ResponseEntity<DatasetBenchmarkResult> uploadAndReplayCsv(
            @RequestParam("file") MultipartFile file,
            @RequestParam(required = false) UUID targetDeviceId
    ) {
        return ResponseEntity.ok(datasetIngestService.replayCustomCsv(file, targetDeviceId));
    }
}
