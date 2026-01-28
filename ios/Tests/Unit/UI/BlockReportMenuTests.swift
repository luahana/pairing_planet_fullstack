import XCTest
import SwiftUI
@testable import Cookstemma

final class BlockReportMenuTests: XCTestCase {

    // MARK: - BlockReportMenu Creation Tests

    func testBlockReportMenu_createsView() {
        let view = BlockReportMenu(
            targetUserId: "user123",
            targetUsername: "testuser",
            onBlock: { },
            onReport: { _ in }
        )
        XCTAssertNotNil(view)
    }

    func testBlockReportMenu_storesTargetUserId() {
        let userId = "user-abc-123"
        let view = BlockReportMenu(
            targetUserId: userId,
            targetUsername: "testuser",
            onBlock: { },
            onReport: { _ in }
        )
        XCTAssertEqual(view.targetUserId, userId)
    }

    func testBlockReportMenu_storesTargetUsername() {
        let username = "chefmaster"
        let view = BlockReportMenu(
            targetUserId: "user123",
            targetUsername: username,
            onBlock: { },
            onReport: { _ in }
        )
        XCTAssertEqual(view.targetUsername, username)
    }

    // MARK: - BlockReportShareMenu Creation Tests

    func testBlockReportShareMenu_createsView() {
        let view = BlockReportShareMenu(
            targetUserId: "user123",
            targetUsername: "testuser",
            shareURL: URL(string: "https://example.com")!,
            onBlock: { },
            onReport: { _ in }
        )
        XCTAssertNotNil(view)
    }

    func testBlockReportShareMenu_storesShareURL() {
        let url = URL(string: "https://cookstemma.com/users/test")!
        let view = BlockReportShareMenu(
            targetUserId: "user123",
            targetUsername: "testuser",
            shareURL: url,
            onBlock: { },
            onReport: { _ in }
        )
        XCTAssertEqual(view.shareURL, url)
    }

    // MARK: - Callback Tests

    func testBlockReportMenu_onBlockCallback_isCalled() {
        var blockCalled = false
        let view = BlockReportMenu(
            targetUserId: "user123",
            targetUsername: "testuser",
            onBlock: { blockCalled = true },
            onReport: { _ in }
        )
        view.onBlock()
        XCTAssertTrue(blockCalled)
    }

    func testBlockReportMenu_onReportCallback_receivesReason() {
        var reportedReason: ReportReason?
        let view = BlockReportMenu(
            targetUserId: "user123",
            targetUsername: "testuser",
            onBlock: { },
            onReport: { reason in reportedReason = reason }
        )
        view.onReport(.spam)
        XCTAssertEqual(reportedReason, .spam)
    }

    func testBlockReportMenu_onReportCallback_allReasons() {
        for expectedReason in ReportReason.allCases {
            var reportedReason: ReportReason?
            let view = BlockReportMenu(
                targetUserId: "user123",
                targetUsername: "testuser",
                onBlock: { },
                onReport: { reason in reportedReason = reason }
            )
            view.onReport(expectedReason)
            XCTAssertEqual(reportedReason, expectedReason)
        }
    }

    // MARK: - ReportReason Tests

    func testReportReason_allCases_haveFiveReasons() {
        XCTAssertEqual(ReportReason.allCases.count, 5)
    }

    func testReportReason_displayText_notEmpty() {
        for reason in ReportReason.allCases {
            XCTAssertFalse(reason.displayText.isEmpty, "\(reason) displayText should not be empty")
        }
    }

    func testReportReason_rawValues_areUppercaseSnakeCase() {
        XCTAssertEqual(ReportReason.spam.rawValue, "SPAM")
        XCTAssertEqual(ReportReason.harassment.rawValue, "HARASSMENT")
        XCTAssertEqual(ReportReason.inappropriateContent.rawValue, "INAPPROPRIATE_CONTENT")
        XCTAssertEqual(ReportReason.impersonation.rawValue, "IMPERSONATION")
        XCTAssertEqual(ReportReason.other.rawValue, "OTHER")
    }
}
