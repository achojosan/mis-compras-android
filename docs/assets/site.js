/* Mis Compras · animaciones de la cartera y el ticket.
   Mejora progresiva: sin JavaScript o con «reducir movimiento» la página
   se ve completa y apilada. El scroll es el nativo del navegador.
   La cartera se fija en escritorio y en móvil, pero solo si el contenido
   de cada tarjeta cabe entero (si no, p. ej. con letra muy grande, se usa
   la variante ligera y nada queda recortado). */
(function () {
  var gsap = window.gsap, ScrollTrigger = window.ScrollTrigger;
  if (!gsap || !ScrollTrigger) return;
  gsap.registerPlugin(ScrollTrigger);
  ScrollTrigger.config({ ignoreMobileResize: true });

  var root = document.documentElement;
  var q = function (s, c) { return (c || document).querySelector(s); };
  var qa = function (s, c) { return Array.prototype.slice.call((c || document).querySelectorAll(s)); };
  var mm;

  function heroIntro() {
    var cards = qa('.hero .hc');
    gsap.from(cards, { y: 70, opacity: 0, duration: 1, ease: 'power3.out', stagger: 0.09, transformOrigin: '50% 100%' });
    gsap.to(cards, {
      yPercent: -14, ease: 'none', stagger: 0.04,
      scrollTrigger: { trigger: '.hero', start: 'top top', end: 'bottom 30%', scrub: true }
    });
  }

  function fits(cards) {
    // el texto debe caber con holgura (en móvil, como mucho la mitad de la tarjeta; en escritorio, la columna completa)
    var narrow = innerWidth < 900;
    return cards.every(function (c) {
      var copy = q('.copy', c);
      return narrow ? copy.offsetHeight <= c.clientHeight * 0.5 : copy.offsetHeight <= c.clientHeight - 40;
    });
  }

  function pinWallet(stage, cards) {
    var STEP = 2, HOLD = 1.2, GAP = 10;
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
        end: function () { return '+=' + Math.round(innerHeight * cards.length * 1.05); },
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
      for (var j = 0; j < i; j++) {
        var k = i - j;
        tl.fromTo(cards[j],
          { y: -(k - 1) * GAP, scale: 1 - (k - 1) * 0.025 },
          { y: -k * GAP, scale: 1 - k * 0.025, duration: 1.1, immediateRender: false }, t);
      }
    });
    tl.to(cards, {
      y: function () { return H() * 1.05; }, rotation: function (i) { return i % 2 ? 2 : -2; },
      scale: 0.96, duration: 1.2, ease: 'power2.in', stagger: { each: 0.06, from: 'end' }
    }, cards.length * STEP + HOLD - 0.4);
  }

  function lightWallet(cards) {
    cards.forEach(function (card, i) {
      gsap.from(card, {
        y: 64, rotation: i % 2 ? 2.5 : -2.5, scale: 0.95, ease: 'none', transformOrigin: '50% 100%',
        scrollTrigger: { trigger: card, start: 'top 94%', end: 'top 56%', scrub: true }
      });
      gsap.fromTo(q('.shot', card), { y: 28 }, {
        y: -12, ease: 'none',
        scrollTrigger: { trigger: card, start: 'top bottom', end: 'bottom top', scrub: true }
      });
    });
  }

  function pinTicket(cuenta, paper) {
    var tl = gsap.timeline({
      defaults: { ease: 'none' },
      scrollTrigger: {
        trigger: cuenta, start: 'top top',
        end: function () { return '+=' + Math.round(innerHeight * 1.3); },
        pin: true, scrub: 0.5, invalidateOnRefresh: true
      }
    });
    tl.fromTo(paper, { yPercent: -100 }, { yPercent: 0, duration: 1 }, 0)
      .from(q('.paper .total strong'), { scale: 1.5, opacity: 0, rotation: -5, duration: 0.18, ease: 'back.out(2)' }, 0.9);
  }

  function lightTicket(paper) {
    gsap.fromTo(paper, { yPercent: -100 }, {
      yPercent: 0, ease: 'none',
      scrollTrigger: { trigger: '.ticket', start: 'top 88%', end: 'top 28%', scrub: true }
    });
  }

  function build() {
    mm = gsap.matchMedia();
    mm.add('(prefers-reduced-motion: no-preference)', function () {
      heroIntro();
      var stage = q('.stage'), cards = qa('.pass', stage);
      var cuenta = q('.cuenta'), paper = q('.paper');

      root.classList.add('pinned');
      var pinned = fits(cards);
      if (pinned) {
        pinWallet(stage, cards);
        if (q('.cuenta-in', cuenta).offsetHeight <= innerHeight * 0.96) pinTicket(cuenta, paper);
        else lightTicket(paper);
      } else {
        root.classList.remove('pinned');
        lightWallet(cards);
        lightTicket(paper);
      }
      return function () { root.classList.remove('pinned'); };
    });
  }

  build();

  // Si cambia el tamaño o el zoom, se vuelve a decidir (p. ej. letra al 200 %)
  var w = innerWidth, h = innerHeight, timer;
  window.addEventListener('resize', function () {
    clearTimeout(timer);
    timer = setTimeout(function () {
      if (innerWidth !== w || Math.abs(innerHeight - h) > 140) {
        w = innerWidth; h = innerHeight;
        mm.revert(); build();
      }
    }, 300);
  });
  window.addEventListener('load', function () { ScrollTrigger.refresh(); });
  if (document.fonts && document.fonts.ready) document.fonts.ready.then(function () { ScrollTrigger.refresh(); });
})();
