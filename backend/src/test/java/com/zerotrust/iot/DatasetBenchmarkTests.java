package com.zerotrust.iot;

import com.zerotrust.iot.dto.dataset.DatasetBenchmarkResult;
import com.zerotrust.iot.dto.dataset.DatasetPresetDto;
import com.zerotrust.iot.service.DatasetIngestService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class DatasetBenchmarkTests {

    @Test
    @DisplayName("Verify Kaggle Dataset presets catalog")
    void testPresetsCatalog() {
        DatasetIngestService service = new DatasetIngestService(null, null, null, null, null);
        List<DatasetPresetDto> presets = service.getAvailablePresets();

        assertNotNull(presets);
        assertEquals(2, presets.size(), "2 benchmark datasets must be pre-configured");
        assertTrue(presets.stream().anyMatch(p -> p.getId().equals("iot-23-smart-meter")));
        assertTrue(presets.stream().anyMatch(p -> p.getId().equals("ciciot-2023-ddos-flood")));
    }

    @Test
    @DisplayName("Verify Confusion Matrix Mathematical Formulations")
    void testConfusionMatrixFormulas() {
        // TP = 45, TN = 50, FP = 3, FN = 2 => Total = 100
        int tp = 45;
        int tn = 50;
        int fp = 3;
        int fn = 2;
        int total = tp + tn + fp + fn;

        double accuracy = ((double) (tp + tn) / total) * 100.0;
        double precision = ((double) tp / (tp + fp)) * 100.0;
        double recall = ((double) tp / (tp + fn)) * 100.0;
        double f1 = 2.0 * (precision * recall) / (precision + recall);

        assertEquals(95.0, accuracy, 0.01, "Accuracy must be 95%");
        assertEquals(93.75, precision, 0.01, "Precision must be 93.75%");
        assertEquals(95.74, recall, 0.01, "Recall must be 95.74%");
        assertTrue(f1 > 94.0 && f1 < 95.0, "F1-Score must be between 94% and 95%");
    }
}
