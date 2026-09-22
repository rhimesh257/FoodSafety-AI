package com.example

import com.example.engine.VerificationEngine
import org.junit.Assert.*
import org.junit.Test

class ExampleUnitTest {
  @Test
  fun addition_isCorrect() {
    assertEquals(4, 2 + 2)
  }

  @Test
  fun structuralImageFilter_validPhoto_passes() {
    val result = VerificationEngine.performStructuralImageFilter(
      photoAngleLabel = "Angle 1: Overhead Dish View",
      brightnessLevel = 0.65f,
      blurMetric = 0.85f
    )
    assertTrue("Valid photo should pass structural filter", result.isValid)
    assertFalse(result.pitchBlackDetected)
    assertFalse(result.blurDetected)
    assertTrue(result.clarityScore >= 70f)
  }

  @Test
  fun structuralImageFilter_pitchBlack_fails() {
    val result = VerificationEngine.performStructuralImageFilter(
      photoAngleLabel = "Angle 1: Overhead Dish View",
      brightnessLevel = 0.05f,
      blurMetric = 0.85f
    )
    assertFalse("Pitch black photo should fail structural filter", result.isValid)
    assertTrue(result.pitchBlackDetected)
    assertNotNull(result.errorMessage)
  }

  @Test
  fun structuralImageFilter_heavyBlur_fails() {
    val result = VerificationEngine.performStructuralImageFilter(
      photoAngleLabel = "Angle 2: Close-up Contamination",
      brightnessLevel = 0.60f,
      blurMetric = 0.20f
    )
    assertFalse("Heavy blur photo should fail structural filter", result.isValid)
    assertTrue(result.blurDetected)
    assertNotNull(result.errorMessage)
  }
}

