/* Mis Compras · animaciones de la cartera y el ticket.
   Mejora progresiva: sin JavaScript o con «reducir movimiento» la página
   se ve completa y apilada. El scroll es el nativo del navegador. */
(function () {
  var gsap = window.gsap, ScrollTrigger = window.ScrollTrigger;
  if (!gsap || !ScrollTrigger) return;
  gsap.registerPlugin(ScrollTrigger);

  var root = document.documentElement;
  var q = function (s, c) { return (c || document).querySelector(s); };
  var qa = function (s, c) { return Array.prototype.slice.call((c || document).querySelectorAll(s)); };
  var mm = gsap.matchMedia();

  function heroIntro() {
    var cards = qa('.hero .hc');
    gsap.from(cards, { y: 70, rotation: 0, opacity: 0, duration: 1, ease: 'power3.out', stagger: 0.09, transformOrigin: '50% 100%' });
    gsap.to(cards, {
      yPercent: -14, ease: 'none', stagger: 0.04,
      scrollTrigger: { trigger: '.hero', start: 'top top', end: 'bottom 30%', scrub: true }
    });
  }

  mm.add(
    {
      full: '(min-width: 900px) and (min-height: 640px) and (prefers-reduced-motion: no-preference)',
      light: '(prefers-reduced-motion: no-preference) and (max-width: 899.98px), (prefers-reduced-motion: no-preference) and (max-height: 639.98px)'
    },
    function (ctx) {
      var full = ctx.conditions.full;
      heroIntro();

      var stage = q('.stage');
      var cards = qa('.pass', stage);
      var paper = q('.paper');
      var lines = qa('.paper li');

      if (full) {
        root.classList.add('pinned');
        var STEP = 2, HOLD = 1.2;
        var count = q('.progress .count b'), name = q('.progress .name');
        var H = function () { return stage.clientHeight; };

        gsap.set(cards, {
          xPercent: -50,
          y: function () { return H() * 1.05; },
          rotation: function (i) { return i % 2 ? 3 : -3; },
          scale: 0.94,
          transformOrigin: '50% 0%'
        });

        var tl = gsap.timeline({
          defaults: { ease: 'power2.out' },
          scrollTrigger: {
            trigger: stage, start: 'top top',
            end: function () { return '+=' + Math.round(innerHeight * 6.4); },
            pin: true, scrub: 0.6, anticipatePin: 1, invalidateOnRefresh: true,
            onUpdate: function (self) {
              var i = Math.min(cards.length - 1, Math.max(0, Math.floor((self.progress * tl.duration() + 0.35) / STEP)));
              if (count.textContent !== String(i + 1)) {
                count.textContent = i + 1;
                name.textContent = cards[i].getAttribute('data-label');
              }
            }
          }
        });

        cards.forEach(function (card, i) {
          var t = i * STEP;
          tl.to(card, { y: 0, rotation: 0, scale: 1, duration: 1.1 }, t);
          // las anteriores se quedan en la cartera, asomando un poco
          for (var j = 0; j < i; j++) {
            var k = i - j;
            tl.fromTo(card.parentNode.children[j],
              { y: -(k - 1) * 16, scale: 1 - (k - 1) * 0.025 },
              { y: -k * 16, scale: 1 - k * 0.025, duration: 1.1, immediateRender: false }, t);
          }
        });
        // la cartera se guarda
        var end = cards.length * STEP + HOLD - 0.4;
        tl.to(cards, {
          y: function () { return H() * 1.05; }, rotation: function (i) { return (i % 2 ? 2 : -2); },
          scale: 0.96, duration: 1.2, ease: 'power2.in', stagger: { each: 0.06, from: 'end' }
        }, end);

        // ticket: se imprime fijado a la pantalla
        var cuenta = q('.cuenta');
        var ttl = gsap.timeline({
          defaults: { ease: 'none' },
          scrollTrigger: {
            trigger: cuenta, start: 'top top',
            end: function () { return '+=' + Math.round(innerHeight * 1.3); },
            pin: true, scrub: 0.5, invalidateOnRefresh: true
          }
        });
        ttl.fromTo(paper, { yPercent: -100 }, { yPercent: 0, duration: 1 }, 0)
           .from(q('.paper .total strong'), { scale: 1.5, opacity: 0, rotation: -5, duration: 0.18, ease: 'back.out(2)' }, 0.9);

        return function () { root.classList.remove('pinned'); };
      }

      /* Variante ligera (móvil / pantallas bajas): sin fijar nada */
      cards.forEach(function (card, i) {
        gsap.from(card, {
          y: 64, rotation: i % 2 ? 2.5 : -2.5, scale: 0.95, ease: 'none', transformOrigin: '50% 100%',
          scrollTrigger: { trigger: card, start: 'top 94%', end: 'top 56%', scrub: true }
        });
        var phone = q('.shot', card);
        gsap.fromTo(phone, { y: 28 }, {
          y: -12, ease: 'none',
          scrollTrigger: { trigger: card, start: 'top bottom', end: 'bottom top', scrub: true }
        });
      });
      gsap.fromTo(paper, { yPercent: -100 }, {
        yPercent: 0, ease: 'none',
        scrollTrigger: { trigger: '.ticket', start: 'top 88%', end: 'top 28%', scrub: true }
      });
    }
  );

  window.addEventListener('load', function () { ScrollTrigger.refresh(); });
  if (document.fonts && document.fonts.ready) document.fonts.ready.then(function () { ScrollTrigger.refresh(); });
})();
