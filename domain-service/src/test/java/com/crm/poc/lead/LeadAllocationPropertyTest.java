package com.crm.poc.lead;

import com.crm.poc.lead.service.LeadAllocationService;
import com.crm.poc.user.model.User;
import com.crm.poc.user.model.UserRole;
import net.jqwik.api.*;
import net.jqwik.api.constraints.IntRange;
import net.jqwik.api.constraints.Size;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * Property-Based Tests for Lead Allocation Algorithm.
 * Tests invariants that must hold for ALL valid inputs.
 */
class LeadAllocationPropertyTest {

    private final LeadAllocationService allocationService = new LeadAllocationService(null, null);

    @Provide
    Arbitrary<List<String>> leadIds() {
        return Arbitraries.integers().between(1, 50)
                .flatMap(count -> Arbitraries.just(
                        IntStream.rangeClosed(1, count)
                                .mapToObj(i -> "LEAD-" + String.format("%03d", i))
                                .collect(Collectors.toList())
                ));
    }

    @Provide
    Arbitrary<List<User>> sortedUsers() {
        return Arbitraries.integers().between(1, 10)
                .flatMap(count -> {
                    List<User> users = IntStream.rangeClosed(1, count)
                            .mapToObj(i -> {
                                User u = new User();
                                u.setId("USR-" + String.format("%03d", i));
                                u.setUsername("user" + String.format("%03d", i));
                                u.setName("User " + i);
                                u.setRole(UserRole.CBBH);
                                return u;
                            })
                            .sorted(Comparator.comparing(u -> u.getUsername().toLowerCase()))
                            .collect(Collectors.toList());
                    return Arbitraries.just(users);
                });
    }

    @Property(tries = 200)
    void sizePreservation(@ForAll("leadIds") List<String> leads, @ForAll("sortedUsers") List<User> users) {
        Map<String, List<String>> result = allocationService.calculateDistribution(leads, users);

        int totalAllocated = result.values().stream().mapToInt(List::size).sum();
        Assertions.assertEquals(leads.size(), totalAllocated,
                "Tổng leads phân bổ phải bằng tổng đầu vào");
    }

    @Property(tries = 200)
    void noLeadDuplication(@ForAll("leadIds") List<String> leads, @ForAll("sortedUsers") List<User> users) {
        Map<String, List<String>> result = allocationService.calculateDistribution(leads, users);

        List<String> allAllocated = result.values().stream()
                .flatMap(List::stream)
                .collect(Collectors.toList());

        Set<String> unique = new HashSet<>(allAllocated);
        Assertions.assertEquals(allAllocated.size(), unique.size(),
                "Không được có lead trùng lặp");
    }

    @Property(tries = 200)
    void fairDistribution(@ForAll("leadIds") List<String> leads, @ForAll("sortedUsers") List<User> users) {
        Map<String, List<String>> result = allocationService.calculateDistribution(leads, users);

        List<Integer> sizes = result.values().stream()
                .map(List::size)
                .collect(Collectors.toList());

        int maxSize = Collections.max(sizes);
        int minSize = Collections.min(sizes);

        Assertions.assertTrue(maxSize - minSize <= 1,
                "Chênh lệch giữa user nhận nhiều nhất và ít nhất phải <= 1");
    }

    @Property(tries = 200)
    void alphabetPriority(@ForAll("leadIds") List<String> leads, @ForAll("sortedUsers") List<User> users) {
        Map<String, List<String>> result = allocationService.calculateDistribution(leads, users);

        int totalLeads = leads.size();
        int totalUsers = users.size();
        int quotient = totalLeads / totalUsers;
        int remainder = totalLeads % totalUsers;

        // Users that should get extra lead (first 'remainder' users alphabetically)
        List<String> userIds = new ArrayList<>(result.keySet());
        for (int i = 0; i < userIds.size(); i++) {
            int expectedCount = quotient + (i < remainder ? 1 : 0);
            int actualCount = result.get(userIds.get(i)).size();
            Assertions.assertEquals(expectedCount, actualCount,
                    "User at position " + i + " should get " + expectedCount + " leads");
        }
    }

    @Property(tries = 200)
    void allUsersReceiveLeads(@ForAll("leadIds") List<String> leads, @ForAll("sortedUsers") List<User> users) {
        Map<String, List<String>> result = allocationService.calculateDistribution(leads, users);

        Assertions.assertEquals(users.size(), result.size(),
                "Tất cả users phải có trong kết quả phân bổ");

        for (User user : users) {
            Assertions.assertTrue(result.containsKey(user.getId()),
                    "User " + user.getId() + " phải có trong kết quả");
        }
    }

    private static class Assertions {
        static void assertEquals(int expected, int actual, String message) {
            if (expected != actual) {
                throw new AssertionError(message + " (expected: " + expected + ", actual: " + actual + ")");
            }
        }

        static void assertTrue(boolean condition, String message) {
            if (!condition) {
                throw new AssertionError(message);
            }
        }
    }
}
