package com.maxed.searchservice;

import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

import java.util.List;

public interface MessageElasticRepository extends ElasticsearchRepository<MessageDoc, String> {
    List<MessageDoc> findByChatIdAndContentContaining(Long chatId, String content);
}
