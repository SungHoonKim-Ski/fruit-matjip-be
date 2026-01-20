package store.onuljang.service;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import store.onuljang.exception.NotFoundException;
import store.onuljang.repository.MessageTemplateRepository;
import store.onuljang.repository.entity.MessageTemplate;
import store.onuljang.repository.entity.enums.MessageType;

@Service
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
@Slf4j
@Transactional(readOnly = true)
public class MessageTemplateService {
    MessageTemplateRepository messageTemplateRepository;

    public MessageTemplate findByMessageType(MessageType type) {
        return messageTemplateRepository.findByMessageType(type)
                .orElseThrow(() -> new NotFoundException("메시지 타입을 찾을 수 없습니다."));
    }
}
