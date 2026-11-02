---
status: accepted
---
# Keep the leveller numbers inside the `DynamicsProcessing` adapter

An architecture review proposed a pure Java description of the processing chain (EQ bands,
threshold, ratio, post-gain, limiter) with a thin adapter that turns it into a
`DynamicsProcessing.Config`, so the numbers could be unit-tested on the JVM. We keep the numbers
where they are: the preset values in the `Preset` enum, the rest in `Leveler`, built straight
into the framework's `Eq`, `Mbc` and `Limiter` objects.

## Considered options

- **Pure chain model plus adapter.** Tests could assert that Night is stronger than Auto, that
  the volume boost stays under 10 dB, that the speech EQ is flat when off. But the only oracle
  for whether a number is right is a person listening on a TV; a JVM test can only restate the
  constants. There is one adapter, so the seam would be hypothetical.
- **Numbers inside the adapter (chosen).** The preset values live in one enum and are testable
  on their own. The remaining pure logic is one addition (makeup gain plus volume boost) and one
  four-entry EQ table. Not enough behaviour to earn a module.

## Consequences

- `Leveler` stays the one place that knows both the numbers and the framework objects.
- Reopen this decision only when a second adapter appears (another effect library, a non-TV
  target) or when the preset numbers start changing often enough that by-ear checks become the
  bottleneck.
