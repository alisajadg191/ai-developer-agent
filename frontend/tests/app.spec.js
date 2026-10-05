import { test, expect } from "@playwright/test";

test("demo report, export, healthy and unknown baselines", async ({ page }) => {
  await page.goto("/");
  await expect(
    page.getByText("Backend connected", { exact: true }),
  ).toBeVisible();
  await page.getByRole("button", { name: /^Investigate/ }).click();
  await expect(
    page.getByRole("heading", { name: "payment-service", exact: true }),
  ).toBeVisible();
  await expect(page.locator(".report .pill")).toHaveText("UNHEALTHY");
  await expect(page.getByText("Demo template", { exact: true })).toBeVisible();
  await expect(page.locator(".evidence")).toContainText(
    "Database connection pool exhausted",
  );
  const downloadPromise = page.waitForEvent("download");
  await page.getByRole("button", { name: "Export JSON" }).click();
  const download = await downloadPromise;
  expect(download.suggestedFilename()).toBe("payment-service-report.json");
  await page.getByLabel("Service name", { exact: true }).fill("order-service");
  await page.getByRole("button", { name: /^Investigate/ }).click();
  await expect(page.locator(".report .pill")).toHaveText("HEALTHY");
  await expect(page.locator(".cause")).toContainText("No failure observed");
  await page
    .getByLabel("Service name", { exact: true })
    .fill("unknown-service");
  await page.getByRole("button", { name: /^Investigate/ }).click();
  await expect(page.locator(".report .pill")).toHaveText("UNKNOWN");
  await expect(page.locator(".evidence-line")).toHaveCount(1);
  expect(
    await page.evaluate(
      () => document.documentElement.scrollWidth <= window.innerWidth,
    ),
  ).toBe(true);
});

test("AI error is visible and does not masquerade as a report", async ({
  page,
}) => {
  await page.goto("/");
  await page.getByRole("button", { name: "Local AI", exact: true }).click();
  await page.route("**/api/ai/investigate", (route) =>
    route.fulfill({
      status: 504,
      contentType: "application/problem+json",
      body: JSON.stringify({
        detail: "The model exceeded the response deadline.",
      }),
    }),
  );
  await page.getByRole("button", { name: /^Investigate/ }).click();
  await expect(page.getByRole("alert")).toContainText("response deadline");
  await expect(page.locator(".report")).toHaveCount(0);
  await expect(
    page.getByRole("button", { name: /^Investigate/ }),
  ).toBeEnabled();
});
