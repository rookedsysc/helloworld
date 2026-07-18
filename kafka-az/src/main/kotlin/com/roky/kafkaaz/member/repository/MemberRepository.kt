package com.roky.kafkaaz.member.repository

import com.roky.kafkaaz.member.domain.Member
import java.time.OffsetDateTime
import org.jooq.DSLContext
import org.jooq.Field
import org.jooq.Record
import org.jooq.Table
import org.jooq.impl.DSL.field
import org.jooq.impl.DSL.name
import org.jooq.impl.DSL.table
import org.springframework.stereotype.Repository
import reactor.core.publisher.Mono

@Repository
class MemberRepository(
    private val dslContext: DSLContext
) {

    fun create(member: Member): Mono<Member> {
        return Mono.from(
            dslContext.insertInto(MEMBERS)
                .set(LOGIN_ID, member.loginId)
                .set(PASSWORD, member.password)
                .returning(*MEMBER_FIELDS)
        ).map(::toMember)
    }

    fun findByLoginId(loginId: String): Mono<Member> {
        return Mono.from(
            dslContext.select(*MEMBER_FIELDS)
                .from(MEMBERS)
                .where(LOGIN_ID.eq(loginId))
        ).map(::toMember)
    }

    private fun toMember(record: Record): Member {
        return Member(
            id = record[ID],
            loginId = record[LOGIN_ID]!!,
            password = record[PASSWORD]!!,
            createdAt = record[CREATED_AT]?.toInstant(),
            updatedAt = record[UPDATED_AT]?.toInstant()
        )
    }

    private companion object {
        private val MEMBERS: Table<Record> = table(name("members"))
        private val ID: Field<Long> = field(name("id"), Long::class.java)
        private val LOGIN_ID: Field<String> = field(name("login_id"), String::class.java)
        private val PASSWORD: Field<String> = field(name("password"), String::class.java)
        private val CREATED_AT: Field<OffsetDateTime> = field(name("created_at"), OffsetDateTime::class.java)
        private val UPDATED_AT: Field<OffsetDateTime> = field(name("updated_at"), OffsetDateTime::class.java)
        private val MEMBER_FIELDS = arrayOf(ID, LOGIN_ID, PASSWORD, CREATED_AT, UPDATED_AT)
    }
}
