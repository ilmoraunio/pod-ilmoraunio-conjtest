# Changelog

This project uses [Break Versioning][breakver]. The version numbers follow a
`<major>.<minor>.<patch>` scheme with the following intent:

| Bump    | Intent                                                     |
| ------- | ---------------------------------------------------------- |
| `major` | Major breaking changes -- check the changelog for details. |
| `minor` | Minor breaking changes -- check the changelog for details. |
| `patch` | No breaking changes, ever!!                                |

`-SNAPSHOT` versions are preview versions for upcoming releases.

[breakver]: https://github.com/ptaoussanis/encore/blob/master/BREAK-VERSIONING.md

## 0.1.0

- BREAKING: Upgrade ilmoraunio/conftest to 0.1.0 [#6](https://github.com/ilmoraunio/pod-ilmoraunio-conjtest/pull/6)
  - The output of HCL2 changes slightly. See open-policy-agent/conftest#1074
    and open-policy-agent/conftest#1006 for more info.

## 0.0.5

- Keywordize EDN keys correctly when using Go parser [#5](https://github.com/ilmoraunio/pod-ilmoraunio-conjtest/pull/5)

## 0.0.4

- Support keyworded keys via `keywordize?` [#3](https://github.com/ilmoraunio/pod-ilmoraunio-conjtest/pull/3)
- fix: incorrect conftest version running for tests [0abc0eb](https://github.com/ilmoraunio/pod-conftest-clj/commit/0abc0ebd8ac857ed3591c3469d4f8a517235111c)
- ci: fail step if tests fail [40a370b](https://github.com/ilmoraunio/pod-conftest-clj/commit/40a370bff17b702600b8f354e60a1b1c9b4111ab)
- ci: drop redundant files & fix tests

## 0.0.3

- Linux arm64 build [#2](https://github.com/ilmoraunio/pod-ilmoraunio-conjtest/pull/2) [f70f8f2](https://github.com/ilmoraunio/pod-conftest-clj/commit/f70f8f2e9b29112b6894e5a289c4f38b5c997513)
- ci: Don't release artifact upon pull request [3539a85](https://github.com/ilmoraunio/pod-conftest-clj/commit/3539a85b2d60b8018653341689daf4815d924d2b)
- ci: Bump checkout to v4 [2fd1d1f](https://github.com/ilmoraunio/pod-conftest-clj/commit/2fd1d1f7bc3dc12904b311e752d31bdb81044f47)
- Use real pod version in README [8083782](https://github.com/ilmoraunio/pod-conftest-clj/commit/80837822839dd90bd8be44b16fc1026987c79811)

## 0.0.2

- Tolerate reader tags
  [03d9be6](https://github.com/ilmoraunio/pod-ilmoraunio-conjtest/commit/03d9be6a26c774412342ef968203c89332e32672)
- ci: WIP
  [7c8d340](https://github.com/ilmoraunio/pod-ilmoraunio-conjtest/commit/7c8d3405c23d1841052cd4d39bf82f2ce3206306)
- Support hidden file retrieval from any dir
  [b7c2019](https://github.com/ilmoraunio/pod-ilmoraunio-conjtest/commit/b7c201994360ade4466c5f6a07e39192c45a4cac)
- Add fn to parse using Go parsers only + enhance docs
  [0d6dee0](https://github.com/ilmoraunio/pod-ilmoraunio-conjtest/commit/0d6dee0d818d361995da7e57f824b26cdcb26e34)
- Fix JSON keys starting with @
  [ae39be7](https://github.com/ilmoraunio/pod-ilmoraunio-conjtest/commit/ae39be7d7cc289ac68d4238ac0a8658c3e508b80)
- Add fns to support specific parsers + improve docs
  [1278611](https://github.com/ilmoraunio/pod-ilmoraunio-conjtest/commit/12786114004784fbc0f2847e3c32df9a8b743107)
- Support directories
  [24934ad](https://github.com/ilmoraunio/pod-ilmoraunio-conjtest/commit/24934adc3ecf08af1daed689ae1ea177c5d48601)
- Add yaml clojure parser
  [6e8d0f3](https://github.com/ilmoraunio/pod-ilmoraunio-conjtest/commit/6e8d0f3d39d1dee8875c1907e1ae0c70ec90d6e0)
- Support YAML multi-documents
  [3577500](https://github.com/ilmoraunio/pod-ilmoraunio-conjtest/commit/35775008002fb73fbc426b5445b966028de39f79)
- Tolerate unknown tags when parsing yaml
  [ccd6be3](https://github.com/ilmoraunio/pod-ilmoraunio-conjtest/commit/ccd6be3662e51f67797111da55cb0d1bf8732f46)
- Support yml files for clojure parser
  [a91fc94](https://github.com/ilmoraunio/pod-ilmoraunio-conjtest/commit/a91fc943af97b79bea352840509954231ef3bb73)
- fix test
  [5e0b2e6](https://github.com/ilmoraunio/pod-ilmoraunio-conjtest/commit/5e0b2e690fcadfa16fa6e33d16609bf7c6b8ed4f)
- Better support for relative paths
  [1c1f330](https://github.com/ilmoraunio/pod-ilmoraunio-conjtest/commit/1c1f3306983d72312aeb751bb4a35afec66a7a7b)
- Fix missing rename
  [db244ca](https://github.com/ilmoraunio/pod-ilmoraunio-conjtest/commit/db244caec6e82cbd5bdc1aaecf5215d690269f02),
  [2127588](https://github.com/ilmoraunio/pod-ilmoraunio-conjtest/commit/21275881a124bb7dd91349e3fb8713340ac42a74)
- Rename pod to pod-ilmoraunio-conjtest
  [1fd40a1](https://github.com/ilmoraunio/pod-ilmoraunio-conjtest/commit/1fd40a1bfd6570d98edd43f655a555972fec0fa6),
  [6c74926](https://github.com/ilmoraunio/pod-ilmoraunio-conjtest/commit/6c749262e53c4015d373686c1f5d71625dbac6f9)
- No difference in perf: pmap->map
  [8c63b46](https://github.com/ilmoraunio/pod-ilmoraunio-conjtest/commit/8c63b46bb7b487501b4dec253af99ea669ab6997)
- Optimization: don't run fs/glob for static paths
  [f9514ac](https://github.com/ilmoraunio/pod-ilmoraunio-conjtest/commit/f9514ac0ae93c50c49af31c73ac06f0371b7d95a)
- Add documentation
  [761c50a](https://github.com/ilmoraunio/pod-ilmoraunio-conjtest/commit/761c50a09c2fba7cbfb4fc4a6ed8a1220e4d1118)
- Fixes to relative & absolute paths
  [773a04d](https://github.com/ilmoraunio/pod-ilmoraunio-conjtest/commit/773a04d0cdcd0a445862a7233d2277091c9a07d8)

## 0.0.1

First release! 🎉
