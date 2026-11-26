## Summary
<!-- Briefly describe what this PR adds, fixes, or changes. -->

## Type of Change
- [ ] `feat`: New feature / mechanic
- [ ] `fix`: Bug fix
- [ ] `refactor`: Code cleanup without behavior change
- [ ] `docs`: Documentation / README update

## Pre-Merge Checklist
- [ ] Ran `.\gradlew.bat runData` if blocks, items, recipes, tags, or translations changed
- [ ] Ran `.\gradlew.bat build` with zero compile errors
- [ ] Smoke-tested gameplay / GUI changes in `.\gradlew.bat runClient`
- [ ] Verified no legacy `ItemStack#getTag()` usage (1.21.1 `DataComponentType` only)
