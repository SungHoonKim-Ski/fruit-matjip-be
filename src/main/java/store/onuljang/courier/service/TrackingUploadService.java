package store.onuljang.courier.service;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import store.onuljang.courier.dto.TrackingUploadError;
import store.onuljang.courier.dto.TrackingUploadResponse;
import store.onuljang.courier.entity.CourierOrder;
import store.onuljang.courier.repository.CourierOrderRepository;
import store.onuljang.shared.entity.enums.CourierCompany;
import store.onuljang.shared.entity.enums.CourierOrderStatus;
import store.onuljang.shared.exception.AdminValidateException;

@Service
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
public class TrackingUploadService {

    CourierOrderRepository courierOrderRepository;

    @Transactional
    public TrackingUploadResponse uploadTracking(MultipartFile file, CourierCompany courierCompany) {
        if (file == null || file.isEmpty()) {
            throw new AdminValidateException("업로드할 파일이 없습니다.");
        }

        List<TrackingUploadError> errors = new ArrayList<>();
        List<ParsedRow> parsedRows = parseExcel(file, errors);

        if (parsedRows.isEmpty() && errors.isEmpty()) {
            throw new AdminValidateException("엑셀 파일에 데이터가 없습니다.");
        }

        if (!errors.isEmpty()) {
            throw new TrackingUploadException("운송장 업로드 중 오류가 발생했습니다.", errors);
        }

        for (ParsedRow parsed : parsedRows) {
            CourierOrder order = courierOrderRepository
                    .findByDisplayCode(parsed.displayCode())
                    .orElse(null);

            if (order == null) {
                errors.add(new TrackingUploadError(
                        parsed.rowNum(), parsed.displayCode(), "존재하지 않는 주문번호입니다."));
                continue;
            }

            if (order.getWaybillNumber() != null && !order.getWaybillNumber().isBlank()) {
                errors.add(new TrackingUploadError(
                        parsed.rowNum(),
                        parsed.displayCode(),
                        "이미 운송장이 등록된 주문입니다. (기존: " + order.getWaybillNumber() + ")"));
                continue;
            }

            if (order.getStatus() != CourierOrderStatus.PAID
                    && order.getStatus() != CourierOrderStatus.PREPARING) {
                errors.add(new TrackingUploadError(
                        parsed.rowNum(),
                        parsed.displayCode(),
                        "발송 처리 불가 상태입니다. (현재 상태: " + order.getStatus() + ")"));
                continue;
            }
        }

        if (!errors.isEmpty()) {
            throw new TrackingUploadException("운송장 업로드 중 오류가 발생했습니다.", errors);
        }

        for (ParsedRow parsed : parsedRows) {
            CourierOrder order = courierOrderRepository
                    .findByDisplayCode(parsed.displayCode())
                    .orElseThrow(() -> new AdminValidateException("주문을 찾을 수 없습니다: " + parsed.displayCode()));
            order.markShipped(parsed.waybillNumber(), courierCompany);
        }

        return new TrackingUploadResponse(parsedRows.size());
    }

    private List<ParsedRow> parseExcel(MultipartFile file, List<TrackingUploadError> errors) {
        List<ParsedRow> result = new ArrayList<>();

        try (InputStream is = file.getInputStream();
                Workbook wb = new XSSFWorkbook(is)) {

            Sheet sheet = wb.getSheetAt(0);
            int lastRow = sheet.getLastRowNum();

            for (int i = 1; i <= lastRow; i++) {
                Row row = sheet.getRow(i);
                if (row == null) {
                    continue;
                }

                String waybillNumber = getCellStringValue(row.getCell(0));
                String displayCode = getCellStringValue(row.getCell(15));

                if (waybillNumber.isBlank() && displayCode.isBlank()) {
                    continue;
                }

                if (displayCode.isBlank()) {
                    errors.add(new TrackingUploadError(i + 1, "", "주문번호(16열)가 비어 있습니다."));
                    continue;
                }

                if (waybillNumber.isBlank()) {
                    errors.add(new TrackingUploadError(i + 1, displayCode, "운송장번호(1열)가 비어 있습니다."));
                    continue;
                }

                result.add(new ParsedRow(i + 1, waybillNumber, displayCode));
            }

        } catch (IOException e) {
            throw new AdminValidateException("엑셀 파일을 읽는 중 오류가 발생했습니다: " + e.getMessage());
        }

        return result;
    }

    private String getCellStringValue(Cell cell) {
        if (cell == null) {
            return "";
        }
        if (cell.getCellType() == CellType.NUMERIC) {
            long numericValue = (long) cell.getNumericCellValue();
            return String.valueOf(numericValue);
        }
        if (cell.getCellType() == CellType.STRING) {
            return cell.getStringCellValue().trim();
        }
        return "";
    }

    private record ParsedRow(int rowNum, String waybillNumber, String displayCode) {}
}
