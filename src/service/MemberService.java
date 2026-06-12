package service;

import model.Member;
import repository.AccountRepository;
import repository.MemberRepository;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public class MemberService {
    private final MemberRepository memberRepository;
    private final AccountRepository accountRepository;

    public MemberService() {
        this.memberRepository = new MemberRepository();
        this.accountRepository = new AccountRepository();
    }

    public List<Member> getAllMembers() {
        return memberRepository.findAll();
    }

    public List<Member> searchMembersByName(String name) {
        return memberRepository.searchByName(name);
    }

    public boolean addMember(Member member) {
        if (memberRepository.findById(member.getAccountId()).isPresent()) {
            return false;
        }
        // Save account first, hardcoding password "123" for now as per legacy code (should hash)
        boolean accountSaved = accountRepository.saveAccount(member.getAccountId(), member.getAccountId(), "123", "member");
        if (accountSaved) {
            return memberRepository.save(member);
        }
        return false;
    }

    public boolean updateMember(Member member, String originalId) {
        if (!member.getAccountId().equals(originalId)) {
            if (memberRepository.findById(member.getAccountId()).isPresent()) {
                return false;
            }
        }
        return memberRepository.update(member, originalId);
    }

    public boolean deleteMember(String id) {
        // Due to CASCADE or sequential deletion, we delete Member then Account, or just Account if CASCADE is configured
        // In the legacy code, it deleted Member then Account.
        accountRepository.deleteById(id); // Member will be deleted via ON DELETE CASCADE defined in schema
        return true;
    }

    public int getTotalMembersCount() {
        return memberRepository.getTotalMembersCount();
    }

    public Map<String, Integer> getMembersRankDistribution() {
        return memberRepository.getMembersRankDistribution();
    }
}
